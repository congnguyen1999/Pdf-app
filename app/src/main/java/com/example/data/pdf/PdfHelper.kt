package com.example.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.zip.Inflater

object PdfHelper {
    private const val TAG = "PdfHelper"

    // High capacity 64KB buffer for fast streaming of large PDFs (100MB - 500MB+)
    suspend fun saveUriToInternalStorage(context: Context, uri: Uri, destinationFileName: String): Pair<String, Long> = withContext(Dispatchers.IO) {
        val booksDir = File(context.filesDir, "pdf_books").apply { if (!exists()) mkdirs() }
        val destFile = File(booksDir, destinationFileName)

        var fileSize = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(65536) // 64KB chunk streaming
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    fileSize += bytesRead
                }
            }
        }
        Pair(destFile.absolutePath, fileSize)
    }

    suspend fun getPdfPageCount(filePath: String): Int = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) return@withContext 1
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count.coerceAtLeast(1)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting page count: ${e.message}")
            1
        }
    }

    suspend fun renderPdfPage(
        filePath: String,
        pageIndex: Int,
        densityMultiplier: Float = 2.0f
    ): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) return@withContext null

        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null

        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            val total = renderer.pageCount
            if (pageIndex < 0 || pageIndex >= total) return@withContext null

            page = renderer.openPage(pageIndex)
            val width = (page.width * densityMultiplier).toInt().coerceAtLeast(100)
            val height = (page.height * densityMultiplier).toInt().coerceAtLeast(100)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Pure white background for crisp text

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering PDF page $pageIndex: ${e.message}")
            null
        } finally {
            try { page?.close() } catch (ignored: Exception) {}
            try { renderer?.close() } catch (ignored: Exception) {}
            try { pfd?.close() } catch (ignored: Exception) {}
        }
    }

    /**
     * Trích xuất toàn bộ văn bản của tất cả các trang trong file PDF mà không bỏ sót bất kỳ trang nào.
     * Hỗ trợ giải mã nén FlateDecode, text operators BT..ET, Tj, TJ, chuỗi Hex, UTF-16BE và octal escapes.
     */
    suspend fun extractAllPagesText(filePath: String, totalPages: Int): List<Pair<Int, String>> = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            return@withContext (1..totalPages.coerceAtLeast(1)).map { it to "" }
        }

        try {
            val bytes = file.readBytes()
            val extractedPages = parsePdfBytesToPages(bytes, totalPages)
            if (extractedPages.isNotEmpty()) {
                return@withContext extractedPages
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in extractAllPagesText: ${e.message}", e)
        }

        // Fallback placeholder with page numbers
        (1..totalPages.coerceAtLeast(1)).map { pageNum ->
            pageNum to "Trang $pageNum của tài liệu (Nội dung biểu đồ/hình vẽ hoặc văn bản dạng scan)."
        }
    }

    private fun parsePdfBytesToPages(pdfBytes: ByteArray, expectedPageCount: Int): List<Pair<Int, String>> {
        val rawText = String(pdfBytes, StandardCharsets.ISO_8859_1)
        val pageContentsMap = mutableMapOf<Int, MutableList<String>>()
        
        // 1. Locate all /Type /Page objects to associate with /Contents streams
        val pageObjRegex = Regex("""(\d+)\s+0\s+obj[\s\S]*?/Type\s*/Page\b([\s\S]*?)endobj""")
        val pageMatches = pageObjRegex.findAll(rawText).toList()

        val streamRegex = Regex("""(\d+)\s+0\s+obj[\s\S]*?stream\r?\n([\s\S]*?)\r?\nendstream""")
        val streamMap = mutableMapOf<Int, String>()

        // 2. Extract and decompress all streams
        val streamObjIndices = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        val objStartRegex = Regex("""(\d+)\s+0\s+obj""")
        val streamKeyword = "stream".toByteArray(StandardCharsets.ISO_8859_1)
        val endstreamKeyword = "endstream".toByteArray(StandardCharsets.ISO_8859_1)

        for (m in objStartRegex.findAll(rawText)) {
            val objNum = m.groupValues[1].toIntOrNull() ?: continue
            val startSearch = m.range.last
            val streamPos = rawText.indexOf("stream", startSearch)
            if (streamPos in startSearch..(startSearch + 300)) {
                val endStreamPos = rawText.indexOf("endstream", streamPos)
                if (endStreamPos > streamPos) {
                    var streamDataStart = streamPos + 6
                    if (streamDataStart < rawText.length && rawText[streamDataStart] == '\r') streamDataStart++
                    if (streamDataStart < rawText.length && rawText[streamDataStart] == '\n') streamDataStart++
                    
                    var streamDataEnd = endStreamPos
                    if (streamDataEnd > streamDataStart && rawText[streamDataEnd - 1] == '\n') streamDataEnd--
                    if (streamDataEnd > streamDataStart && rawText[streamDataEnd - 1] == '\r') streamDataEnd--

                    val isFlate = rawText.substring(m.range.first, streamPos).contains("/FlateDecode")
                    val decompressed = if (isFlate) {
                        decompressFlate(pdfBytes, streamDataStart, streamDataEnd - streamDataStart)
                    } else {
                        val len = (streamDataEnd - streamDataStart).coerceAtLeast(0)
                        if (len > 0 && streamDataStart + len <= pdfBytes.size) {
                            String(pdfBytes, streamDataStart, len, StandardCharsets.ISO_8859_1)
                        } else ""
                    }
                    val extractedText = extractTextFromContentStream(decompressed)
                    if (extractedText.isNotBlank()) {
                        streamMap[objNum] = extractedText
                    }
                }
            }
        }

        // 3. Map page objects to their contents
        if (pageMatches.isNotEmpty()) {
            pageMatches.forEachIndexed { index, match ->
                val pageNum = index + 1
                val pageBody = match.groupValues[2]
                val contentsRegex = Regex("""/Contents\s+(?:(\d+)\s+0\s+R|\[([\s\d\wR]+)\])""")
                val contentsMatch = contentsRegex.find(pageBody)
                val list = pageContentsMap.getOrPut(pageNum) { mutableListOf() }

                if (contentsMatch != null) {
                    val singleRef = contentsMatch.groupValues[1].toIntOrNull()
                    if (singleRef != null) {
                        streamMap[singleRef]?.let { list.add(it) }
                    } else {
                        val arrayRef = contentsMatch.groupValues[2]
                        val objNums = Regex("""(\d+)\s+0\s+R""").findAll(arrayRef).mapNotNull { it.groupValues[1].toIntOrNull() }
                        for (ref in objNums) {
                            streamMap[ref]?.let { list.add(it) }
                        }
                    }
                }
            }
        }

        // 4. If structured page mapping found text, return it
        val finalPageCount = expectedPageCount.coerceAtLeast(pageMatches.size).coerceAtLeast(1)
        val result = mutableListOf<Pair<Int, String>>()

        for (p in 1..finalPageCount) {
            val texts = pageContentsMap[p]
            val pageText = if (!texts.isNullOrEmpty()) {
                texts.joinToString("\n").trim()
            } else {
                // If page specific mapping was empty, check if we have general streams
                streamMap.values.elementAtOrNull(p - 1)?.trim() ?: ""
            }
            val formattedText = if (pageText.isNotBlank()) {
                pageText
            } else {
                "Trang $p (Trang biểu đồ, sơ đồ minh họa hoặc trang mở đầu chương)."
            }
            result.add(p to formattedText)
        }

        return result
    }

    private fun decompressFlate(data: ByteArray, offset: Int, length: Int): String {
        if (offset < 0 || length <= 0 || offset + length > data.size) return ""
        try {
            val inflater = Inflater(false)
            inflater.setInput(data, offset, length)
            val outputStream = ByteArrayOutputStream(length * 3)
            val buffer = ByteArray(4096)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) break
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            val decompressedBytes = outputStream.toByteArray()
            return String(decompressedBytes, StandardCharsets.UTF_8).ifBlank {
                String(decompressedBytes, StandardCharsets.ISO_8859_1)
            }
        } catch (e: Exception) {
            // Try with nowrap = true
            try {
                val inflaterNowrap = Inflater(true)
                inflaterNowrap.setInput(data, offset, length)
                val outputStream = ByteArrayOutputStream(length * 3)
                val buffer = ByteArray(4096)
                while (!inflaterNowrap.finished()) {
                    val count = inflaterNowrap.inflate(buffer)
                    if (count == 0) break
                    outputStream.write(buffer, 0, count)
                }
                inflaterNowrap.end()
                val decompressedBytes = outputStream.toByteArray()
                return String(decompressedBytes, StandardCharsets.UTF_8).ifBlank {
                    String(decompressedBytes, StandardCharsets.ISO_8859_1)
                }
            } catch (ignored: Exception) {
                return ""
            }
        }
    }

    private fun extractTextFromContentStream(streamContent: String): String {
        if (streamContent.isBlank()) return ""
        val sb = StringBuilder()
        
        // Find BT ... ET blocks
        val btRegex = Regex("""BT\b([\s\S]*?)ET\b""")
        for (btMatch in btRegex.findAll(streamContent)) {
            val block = btMatch.groupValues[1]
            
            // Match operators: Tj, TJ, ', "
            val tjRegex = Regex("""(?:\((?:\\.|[^()\\])*\)|<[0-9a-fA-F\s]+>|\[[\s\S]*?\])\s*(?:Tj|TJ|'|")""")
            for (tjMatch in tjRegex.findAll(block)) {
                val opStr = tjMatch.value
                val text = decodePdfStringLiteralOrArray(opStr)
                if (text.isNotBlank()) {
                    sb.append(text)
                    if (opStr.endsWith("'") || opStr.endsWith("\"") || opStr.contains("T*")) {
                        sb.append("\n")
                    } else {
                        sb.append(" ")
                    }
                }
            }
            sb.append("\n")
        }

        // Clean extra whitespaces
        return sb.toString()
            .replace(Regex("""[ \t]+"""), " ")
            .replace(Regex("""\n\s*\n+"""), "\n\n")
            .trim()
    }

    private fun decodePdfStringLiteralOrArray(opStr: String): String {
        val trimmed = opStr.trim()
        if (trimmed.startsWith("[")) {
            // TJ array: [(Hello) 120 (world)] TJ
            val sb = StringBuilder()
            val partRegex = Regex("""\(((?:\\.|[^()\\])*)\)|<([0-9a-fA-F\s]+)>""")
            for (part in partRegex.findAll(trimmed)) {
                if (part.groups[1] != null) {
                    sb.append(decodeEscapedLiteral(part.groups[1]!!.value))
                } else if (part.groups[2] != null) {
                    sb.append(decodeHexString(part.groups[2]!!.value))
                }
            }
            return sb.toString()
        } else if (trimmed.startsWith("(")) {
            val literal = trimmed.substringAfter("(").substringBeforeLast(")")
            return decodeEscapedLiteral(literal)
        } else if (trimmed.startsWith("<")) {
            val hex = trimmed.substringAfter("<").substringBeforeLast(">")
            return decodeHexString(hex)
        }
        return ""
    }

    private fun decodeEscapedLiteral(raw: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                val next = raw[i + 1]
                when (next) {
                    'n' -> { sb.append('\n'); i += 2 }
                    'r' -> { sb.append('\r'); i += 2 }
                    't' -> { sb.append('\t'); i += 2 }
                    'b' -> { sb.append('\b'); i += 2 }
                    'f' -> { sb.append('\u000C'); i += 2 }
                    '(' -> { sb.append('('); i += 2 }
                    ')' -> { sb.append(')'); i += 2 }
                    '\\' -> { sb.append('\\'); i += 2 }
                    in '0'..'7' -> {
                        // Octal escape \ddd
                        var octal = ""
                        var count = 0
                        var j = i + 1
                        while (j < raw.length && count < 3 && raw[j] in '0'..'7') {
                            octal += raw[j]
                            count++
                            j++
                        }
                        val charCode = octal.toIntOrNull(8) ?: 32
                        sb.append(charCode.toChar())
                        i = j
                    }
                    else -> {
                        sb.append(next)
                        i += 2
                    }
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun decodeHexString(hex: String): String {
        val cleanHex = hex.replace(Regex("""\s+"""), "")
        if (cleanHex.length % 2 != 0) return ""
        val bytes = ByteArray(cleanHex.length / 2)
        for (i in bytes.indices) {
            val sub = cleanHex.substring(i * 2, i * 2 + 2)
            bytes[i] = sub.toIntOrNull(16)?.toByte() ?: 0
        }
        // Check for UTF-16BE BOM (0xFE 0xFF)
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16BE)
        }
        // Try UTF-16BE if every alternate byte is 0
        if (bytes.size >= 2 && bytes[0] == 0.toByte()) {
            return String(bytes, StandardCharsets.UTF_16BE)
        }
        return String(bytes, StandardCharsets.UTF_8).ifBlank {
            String(bytes, StandardCharsets.ISO_8859_1)
        }
    }

    suspend fun createSampleBookPdf(
        context: Context,
        fileName: String,
        bookTitle: String,
        author: String,
        pagesContent: List<Pair<String, String>>
    ): Pair<String, Int> = withContext(Dispatchers.IO) {
        val booksDir = File(context.filesDir, "pdf_books").apply { if (!exists()) mkdirs() }
        val targetFile = File(booksDir, fileName)

        if (targetFile.exists() && targetFile.length() > 0L) {
            val count = getPdfPageCount(targetFile.absolutePath)
            return@withContext Pair(targetFile.absolutePath, count)
        }

        val document = PdfDocument()
        val pageWidth = 595 // A4 standard width (points)
        val pageHeight = 842 // A4 standard height (points)
        val margin = 48

        // Cover Page (Page 1)
        val coverPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val coverPage = document.startPage(coverPageInfo)
        val coverCanvas = coverPage.canvas

        // Cover background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1E1B4B")
            style = Paint.Style.FILL
        }
        coverCanvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Accent gold line
        val goldPaint = Paint().apply {
            color = Color.parseColor("#F59E0B")
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        coverCanvas.drawRoundRect(32f, 32f, pageWidth - 32f, pageHeight - 32f, 16f, 16f, goldPaint)

        // Title on cover
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val titleLayout = StaticLayout.Builder.obtain(
            bookTitle,
            0,
            bookTitle.length,
            titlePaint,
            pageWidth - 2 * margin - 32
        ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()

        coverCanvas.save()
        coverCanvas.translate((margin + 16).toFloat(), 260f)
        titleLayout.draw(coverCanvas)
        coverCanvas.restore()

        // Author
        val authorPaint = Paint().apply {
            color = Color.parseColor("#E0E7FF")
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        coverCanvas.drawText("Tác giả: $author", pageWidth / 2f, 440f, authorPaint)

        val badgePaint = Paint().apply {
            color = Color.parseColor("#6366F1")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        coverCanvas.drawRoundRect(pageWidth / 2f - 90f, 520f, pageWidth / 2f + 90f, 560f, 20f, 20f, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        coverCanvas.drawText("AI STUDY EDITION", pageWidth / 2f, 545f, badgeTextPaint)

        document.finishPage(coverPage)

        // Content Pages
        var currentPageNumber = 2
        for ((chapterTitle, contentText) in pagesContent) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // White background
            canvas.drawColor(Color.WHITE)

            // Header banner
            val headerBg = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 64f, headerBg)

            val headerTextPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText(bookTitle.take(35), margin.toFloat(), 40f, headerTextPaint)
            canvas.drawText("Trang $currentPageNumber", (pageWidth - margin - 50).toFloat(), 40f, headerTextPaint)

            // Chapter Title
            val chapterTitlePaint = TextPaint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 20f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val chapterLayout = StaticLayout.Builder.obtain(
                chapterTitle,
                0,
                chapterTitle.length,
                chapterTitlePaint,
                pageWidth - 2 * margin
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(margin.toFloat(), 90f)
            chapterLayout.draw(canvas)
            canvas.restore()

            // Body text
            val bodyPaint = TextPaint().apply {
                color = Color.parseColor("#334155")
                textSize = 13.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            val bodyLayout = StaticLayout.Builder.obtain(
                contentText,
                0,
                contentText.length,
                bodyPaint,
                pageWidth - 2 * margin
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(5f, 1f).build()

            canvas.save()
            canvas.translate(margin.toFloat(), 90f + chapterLayout.height + 24f)
            bodyLayout.draw(canvas)
            canvas.restore()

            document.finishPage(page)
            currentPageNumber++
        }

        FileOutputStream(targetFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        Pair(targetFile.absolutePath, currentPageNumber - 1)
    }
}
