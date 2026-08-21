package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.api.UniversalAiService
import com.example.data.local.AiPreferencesManager
import com.example.data.local.AppDatabase
import com.example.data.local.SampleBookData
import com.example.data.model.AiProviderConfig
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.data.pdf.PdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class BookStudyRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val aiService: UniversalAiService = UniversalAiService(),
    val aiPreferencesManager: AiPreferencesManager = AiPreferencesManager(context)
) {
    private val bookDao = database.bookDao()
    private val flashcardDao = database.flashcardDao()
    private val quizDao = database.quizDao()
    private val chatDao = database.chatDao()
    private val studyPlanDao = database.studyPlanDao()

    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun initDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existing = bookDao.getAllBooks().first()
        if (existing.isEmpty()) {
            SampleBookData.populateSampleBooks(context, database)
        }
    }

    fun getBook(bookId: Long): Flow<BookEntity?> = bookDao.getBookById(bookId)
    suspend fun getBookOnce(bookId: Long): BookEntity? = bookDao.getBookByIdOnce(bookId)

    fun getChapters(bookId: Long): Flow<List<ChapterEntity>> = bookDao.getChaptersByBookId(bookId)
    fun getFlashcards(bookId: Long): Flow<List<FlashcardEntity>> = flashcardDao.getFlashcardsByBookId(bookId)
    fun getQuizQuestions(bookId: Long): Flow<List<QuizQuestionEntity>> = quizDao.getQuizQuestionsByBookId(bookId)
    fun getChatMessages(bookId: Long): Flow<List<ChatMessageEntity>> = chatDao.getChatMessages(bookId)
    fun getStudyPlans(bookId: Long): Flow<List<StudyPlanEntity>> = studyPlanDao.getStudyPlans(bookId)
    fun getPages(bookId: Long): Flow<List<com.example.data.model.BookPageEntity>> = bookDao.getPagesByBookId(bookId)
    suspend fun getPage(bookId: Long, pageNumber: Int): com.example.data.model.BookPageEntity? = bookDao.getPageByNumber(bookId, pageNumber)

    suspend fun importPdfBook(
        uri: Uri,
        fileName: String,
        customTitle: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val cleanTitle = (customTitle?.takeIf { it.isNotBlank() }
            ?: fileName.removeSuffix(".pdf").replace("_", " ").replace("-", " "))
            .trim()

        val destName = "book_${System.currentTimeMillis()}_${fileName.filter { it.isLetterOrDigit() || it == '.' }}"
        val (filePath, fileSize) = PdfHelper.saveUriToInternalStorage(context, uri, destName)
        val pageCount = PdfHelper.getPdfPageCount(filePath)

        val colors = listOf("#4F46E5", "#0D9488", "#D97706", "#DC2626", "#7C3AED", "#2563EB", "#059669")
        val chosenColor = colors[((System.currentTimeMillis() % colors.size).toInt())]

        val book = BookEntity(
            title = cleanTitle,
            author = "Tài liệu PDF",
            totalPages = pageCount,
            fileSize = fileSize,
            filePath = filePath,
            isSample = false,
            coverColorHex = chosenColor,
            summary = "Cuốn sách PDF '$cleanTitle' ($pageCount trang, ${fileSize / 1024} KB) đã được nạp thành công. Nhấn nút 'Phân Tích AI' để đọc toàn diện 100% tất cả các trang, tóm tắt các chương và tạo bài tập ôn tập.",
            coreThemes = "Tài liệu học tập toàn văn",
            keyTakeaways = "• Sách có $pageCount trang đã được giải mã và lưu trữ đầy đủ từng trang để học tập.",
            studyProgress = 0
        )

        val bookId = bookDao.insertBook(book)

        // Thoroughly extract and index text for EVERY page
        val extractedPages = PdfHelper.extractAllPagesText(filePath, pageCount)
        val pageEntities = extractedPages.map { (pageNum, text) ->
            com.example.data.model.BookPageEntity(
                bookId = bookId,
                pageNumber = pageNum,
                textContent = text,
                wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            )
        }
        bookDao.insertPages(pageEntities)

        // Seed initial welcoming message
        chatDao.insertMessage(
            ChatMessageEntity(
                bookId = bookId,
                sender = "gemini",
                content = "Chào bạn! Tôi là Gia sư AI cho cuốn sách '$cleanTitle' ($pageCount trang). Tôi đã nạp toàn bộ $pageCount trang của tệp PDF này. Bạn có thể hỏi tôi bất kỳ câu hỏi nào về nội dung chi tiết trong sách, yêu cầu giải thích trang cụ thể hoặc tạo bài kiểm tra!"
            )
        )

        bookId
    }

    suspend fun updateBook(book: BookEntity) = bookDao.updateBook(book)
    suspend fun updateProgress(bookId: Long, progress: Int) = bookDao.updateProgress(bookId, progress)

    suspend fun deleteBook(bookId: Long) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId)
        if (book != null && book.filePath.isNotBlank()) {
            try {
                val f = File(book.filePath)
                if (f.exists() && !book.isSample) {
                    f.delete()
                }
            } catch (ignored: Exception) {}
        }
        bookDao.deleteBookById(bookId)
        bookDao.deleteChaptersByBookId(bookId)
        bookDao.deletePagesByBookId(bookId)
        flashcardDao.deleteFlashcardsByBookId(bookId)
        quizDao.deleteQuizByBookId(bookId)
        chatDao.clearChatHistory(bookId)
        studyPlanDao.deleteStudyPlans(bookId)
    }

    suspend fun toggleFlashcardMastered(flashcard: FlashcardEntity) {
        flashcardDao.updateMasteryStatus(flashcard.id, !flashcard.isMastered)
    }

    suspend fun answerQuizQuestion(questionId: Long, selectedIndex: Int, isCorrect: Boolean) {
        quizDao.recordAnswer(questionId, selectedIndex, isCorrect)
    }

    suspend fun resetQuiz(bookId: Long) {
        quizDao.resetQuizAnswers(bookId)
    }

    suspend fun togglePlanCompletion(planId: Long, currentStatus: Boolean) {
        studyPlanDao.updatePlanCompletion(planId, !currentStatus)
    }

    suspend fun sendChatMessage(
        bookId: Long,
        userMessage: String,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): String = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext "Không tìm thấy sách"
        val pages = bookDao.getPagesByBookIdOnce(bookId)
        
        chatDao.insertMessage(
            ChatMessageEntity(
                bookId = bookId,
                sender = "user",
                content = userMessage
            )
        )

        val history = chatDao.getChatMessages(bookId).first()
        val bookContext = buildString {
            appendLine("=== THÔNG TIN SÁCH ===")
            appendLine("Tiêu đề: ${book.title}")
            appendLine("Tác giả: ${book.author}")
            appendLine("Tổng số trang: ${book.totalPages}")
            appendLine("Tóm tắt: ${book.summary}")
            appendLine("Điểm cốt lõi: ${book.keyTakeaways}")
            appendLine()
            appendLine("=== TOÀN BỘ NỘI DUNG TỪNG TRANG (${pages.size} TRANG) ===")
            for (p in pages) {
                appendLine("--- [TRANG ${p.pageNumber}] ---")
                appendLine(p.textContent.take(1500))
            }
        }

        val result = aiService.askTutor(
            bookTitle = book.title,
            bookContext = bookContext,
            chatHistory = history,
            userMessage = userMessage,
            config = config
        )

        val reply = result.getOrElse { e ->
            "Xin lỗi, đã xảy ra lỗi từ ${config.provider.displayName}: ${e.message}. Hãy kiểm tra lại API Key và cấu hình trong cài đặt."
        }

        chatDao.insertMessage(
            ChatMessageEntity(
                bookId = bookId,
                sender = "gemini",
                content = reply
            )
        )

        reply
    }

    suspend fun generateAiSummaryAndChapters(
        bookId: Long,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext Result.failure(Exception("Không tìm thấy sách"))
        val pages = bookDao.getPagesByBookIdOnce(bookId)
        
        val fullPagesText = buildString {
            appendLine("Cuốn sách: ${book.title} (Tổng số ${book.totalPages} trang)")
            appendLine("Dưới đây là toàn bộ văn bản từ trang 1 đến trang ${pages.size}. Hãy đọc kỹ từng trang, không bỏ sót bất kỳ trang nào:")
            for (p in pages) {
                appendLine("--- [TRANG ${p.pageNumber}] ---")
                appendLine(p.textContent.ifBlank { "(Trang đồ họa / minh họa)" })
                appendLine()
            }
        }

        val result = aiService.generateBookOverview(book.title, fullPagesText, config)
        
        result.map { dto ->
            val updatedBook = book.copy(
                author = if (dto.author.isNotBlank() && dto.author != "Tác giả" && dto.author != "Tài liệu PDF") dto.author else book.author,
                summary = dto.overview,
                coreThemes = dto.coreThemes.joinToString(", "),
                keyTakeaways = dto.keyTakeaways.joinToString("\n• ", prefix = "• "),
                studyProgress = (book.studyProgress + 20).coerceAtMost(100)
            )
            bookDao.updateBook(updatedBook)

            if (dto.chapters.isNotEmpty()) {
                bookDao.deleteChaptersByBookId(bookId)
                val chapters = dto.chapters.mapIndexed { idx, ch ->
                    val startPg = if (ch.chapterNumber > 0 && ch.chapterNumber <= book.totalPages) ch.chapterNumber else (idx * 2 + 1).coerceAtMost(book.totalPages)
                    val endPg = (startPg + 2).coerceAtMost(book.totalPages)
                    ChapterEntity(
                        bookId = bookId,
                        chapterNumber = ch.chapterNumber.takeIf { it > 0 } ?: (idx + 1),
                        title = ch.title,
                        startPage = startPg,
                        endPage = endPg,
                        summary = ch.summary,
                        keyTakeaways = ch.keyPoints.joinToString("\n• ", prefix = "• ")
                    )
                }
                bookDao.insertChapters(chapters)
            }
        }
    }

    suspend fun generateAiFlashcards(
        bookId: Long,
        count: Int = 6,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext Result.failure(Exception("Không tìm thấy sách"))
        val pages = bookDao.getPagesByBookIdOnce(bookId)
        val chapters = bookDao.getChaptersByBookIdOnce(bookId)
        
        val fullText = buildString {
            appendLine("Sách: ${book.title}")
            appendLine("Tóm tắt: ${book.summary}")
            if (chapters.isNotEmpty()) {
                appendLine("Danh sách chương: ${chapters.joinToString { it.title }}")
            }
            appendLine("Nội dung chi tiết từng trang:")
            for (p in pages) {
                appendLine("--- Trang ${p.pageNumber}: ${p.textContent.take(800)}")
            }
        }

        val result = aiService.generateFlashcards(book.title, fullText, count, config)
        result.map { cards ->
            val entities = cards.map { card ->
                FlashcardEntity(
                    bookId = bookId,
                    chapterTitle = card.chapter.ifBlank { "Tổng quát" },
                    front = card.front,
                    back = card.back,
                    keyConcept = card.concept,
                    difficulty = card.difficulty,
                    isMastered = false
                )
            }
            flashcardDao.insertFlashcards(entities)
        }
    }

    suspend fun generateAiQuiz(
        bookId: Long,
        count: Int = 5,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext Result.failure(Exception("Không tìm thấy sách"))
        val pages = bookDao.getPagesByBookIdOnce(bookId)
        val chapters = bookDao.getChaptersByBookIdOnce(bookId)

        val fullText = buildString {
            appendLine("Sách: ${book.title}")
            appendLine("Tóm tắt: ${book.summary}")
            appendLine("Bài học cốt lõi: ${book.keyTakeaways}")
            if (chapters.isNotEmpty()) {
                appendLine("Các chương: ${chapters.joinToString { it.title }}")
            }
            appendLine("Toàn bộ nội dung các trang:")
            for (p in pages) {
                appendLine("--- Trang ${p.pageNumber}: ${p.textContent.take(800)}")
            }
        }

        val result = aiService.generateQuiz(book.title, fullText, count, config)
        result.map { questions ->
            val entities = questions.map { q ->
                val opts = q.options + listOf("", "", "", "")
                QuizQuestionEntity(
                    bookId = bookId,
                    question = q.question,
                    optionA = opts.getOrElse(0) { "Phương án A" },
                    optionB = opts.getOrElse(1) { "Phương án B" },
                    optionC = opts.getOrElse(2) { "Phương án C" },
                    optionD = opts.getOrElse(3) { "Phương án D" },
                    correctOptionIndex = q.correctIndex.coerceIn(0, 3),
                    explanation = q.explanation,
                    referenceChapter = q.reference
                )
            }
            quizDao.insertQuizQuestions(entities)
        }
    }

    suspend fun generateAiStudyPlan(
        bookId: Long,
        days: Int = 7,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext Result.failure(Exception("Không tìm thấy sách"))
        val chapters = bookDao.getChaptersByBookIdOnce(bookId)
        val pages = bookDao.getPagesByBookIdOnce(bookId)

        val fullText = buildString {
            appendLine("Sách: ${book.title}")
            appendLine("Tóm tắt: ${book.summary}")
            appendLine("Chủ đề cốt lõi: ${book.coreThemes}")
            if (chapters.isNotEmpty()) {
                appendLine("Các chương:")
                chapters.forEach { appendLine("- Chương ${it.chapterNumber}: ${it.title} (Trang ${it.startPage}-${it.endPage}): ${it.summary}") }
            }
            appendLine("Tổng số trang: ${pages.size}")
        }

        val result = aiService.generateStudyPlan(book.title, fullText, days, config)
        result.map { planDays ->
            studyPlanDao.deleteStudyPlans(bookId)
            val entities = planDays.map { d ->
                StudyPlanEntity(
                    bookId = bookId,
                    title = "Lộ trình ${days} Ngày Làm Chủ",
                    dayNumber = d.day,
                    dayTitle = d.title,
                    focusGoal = d.goal,
                    actionItems = d.tasks.joinToString("\n• ", prefix = "• "),
                    isCompleted = false
                )
            }
            studyPlanDao.insertStudyPlans(entities)
        }
    }

    suspend fun explainPage(
        bookId: Long,
        pageNumber: Int,
        config: AiProviderConfig = aiPreferencesManager.getActiveConfig()
    ): Result<String> = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId) ?: return@withContext Result.failure(Exception("Không tìm thấy sách"))
        val page = bookDao.getPageByNumber(bookId, pageNumber)
        
        val pageText = page?.textContent?.takeIf { it.isNotBlank() }
            ?: "Trang $pageNumber trong cuốn '${book.title}' (${book.totalPages} trang)."

        val contextInfo = buildString {
            appendLine("Nội dung thực tế của Trang $pageNumber:")
            appendLine(pageText)
            appendLine()
            appendLine("Bối cảnh chung cuốn sách '${book.title}': ${book.summary.take(300)}")
        }

        aiService.explainPageContent(book.title, pageNumber, contextInfo, config)
    }

    suspend fun testConnection(config: AiProviderConfig): Result<String> {
        return aiService.testAiConnection(config)
    }
}
