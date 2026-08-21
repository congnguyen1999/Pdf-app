package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeminiApiRequest
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiPart
import com.example.data.model.GeneratedChapterDto
import com.example.data.model.GeneratedFlashcardDto
import com.example.data.model.GeneratedQuestionDto
import com.example.data.model.GeneratedStudyDayDto
import com.example.data.model.GeneratedSummaryDto
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class GeminiStudyService(
    private val apiService: GeminiApiService = GeminiApiClient.service
) {
    companion object {
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        private const val TAG = "GeminiStudyService"
    }

    private fun resolveApiKey(customKey: String?): String {
        return if (!customKey.isNullOrBlank()) {
            customKey.trim()
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }
    }

    suspend fun generateBookOverview(
        bookTitle: String,
        sampleText: String,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<GeneratedSummaryDto> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cấu hình GEMINI_API_KEY trong Cài đặt hoặc Secrets Panel."))
        }

        val prompt = """
            Bạn là một chuyên gia giáo dục và tóm tắt sách hàng đầu thế giới.
            Hãy phân tích cuốn sách '$bookTitle' (dựa trên thông tin và nội dung sau):
            ---
            $sampleText
            ---
            
            Hãy trả về kết quả định dạng JSON thuần túy (không kèm markdown ```json bọc ngoài nếu có thể, hoặc bọc trong ```json) theo cấu trúc chính xác sau:
            {
              "title": "$bookTitle",
              "author": "Tên tác giả",
              "overview": "Tóm tắt tổng quan súc tích, truyền cảm hứng về cuốn sách (khoảng 3-4 đoạn văn)",
              "coreThemes": ["Chủ đề chính 1", "Chủ đề chính 2", "Chủ đề chính 3", "Chủ đề chính 4"],
              "keyTakeaways": ["Bài học thực tiễn 1", "Bài học thực tiễn 2", "Bài học thực tiễn 3", "Bài học thực tiễn 4", "Bài học thực tiễn 5"],
              "chapters": [
                {
                  "chapterNumber": 1,
                  "title": "Tên chương 1",
                  "summary": "Tóm tắt nội dung cốt lõi của chương 1",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                },
                {
                  "chapterNumber": 2,
                  "title": "Tên chương 2",
                  "summary": "Tóm tắt nội dung cốt lõi của chương 2",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                },
                {
                  "chapterNumber": 3,
                  "title": "Tên chương 3",
                  "summary": "Tóm tắt nội dung cốt lõi của chương 3",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                }
              ]
            }
            Chú ý: Toàn bộ nội dung trả về bằng tiếng Việt rõ ràng, sâu sắc, chuẩn xác.
        """.trimIndent()

        try {
            val responseText = executePrompt(prompt, apiKey, model)
            val cleanJson = extractJsonString(responseText)
            val adapter = GeminiApiClient.moshi.adapter(GeneratedSummaryDto::class.java)
            val result = adapter.fromJson(cleanJson)
                ?: parseSummaryManually(cleanJson, bookTitle)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating overview", e)
            Result.failure(e)
        }
    }

    suspend fun generateFlashcards(
        bookTitle: String,
        sampleText: String,
        count: Int = 6,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<List<GeneratedFlashcardDto>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cung cấp Gemini API Key để tạo Flashcards."))
        }

        val prompt = """
            Tạo $count thẻ ghi nhớ (Flashcards) chất lượng cao để ôn tập cuốn sách '$bookTitle'.
            Nội dung sách:
            $sampleText
            
            Mỗi thẻ cần có:
            - front: Câu hỏi gợi nhớ sâu hoặc khái niệm cần định nghĩa
            - back: Lời giải thích súc tích, trực diện, dễ nhớ kèm ví dụ (nếu có)
            - concept: Tên khái niệm cốt lõi (1-3 từ)
            - difficulty: "Dễ", "Vừa" hoặc "Khó"
            - chapter: Tên chương hoặc phần liên quan
            
            Trả về định dạng JSON mảng (Array of objects):
            [
              {
                "front": "...",
                "back": "...",
                "concept": "...",
                "difficulty": "Vừa",
                "chapter": "..."
              }
            ]
            Bằng tiếng Việt.
        """.trimIndent()

        try {
            val responseText = executePrompt(prompt, apiKey, model)
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedFlashcardDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedFlashcardDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseFlashcardsManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating flashcards", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(
        bookTitle: String,
        sampleText: String,
        count: Int = 5,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<List<GeneratedQuestionDto>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cung cấp Gemini API Key để tạo bài kiểm tra."))
        }

        val prompt = """
            Tạo bộ $count câu hỏi trắc nghiệm khách quan 4 phương án (A, B, C, D) kiểm tra mức độ hiểu sâu cuốn sách '$bookTitle'.
            Nội dung sách:
            $sampleText
            
            Mỗi câu hỏi phải có tính ứng dụng, phản biện, không chỉ học vẹt.
            Định dạng JSON trả về dạng mảng:
            [
              {
                "question": "Nội dung câu hỏi...",
                "options": ["Phương án A", "Phương án B", "Phương án C", "Phương án D"],
                "correctIndex": 0,
                "explanation": "Giải thích chi tiết tại sao đáp án này đúng dựa trên nội dung sách...",
                "reference": "Chương hoặc phần liên quan"
              }
            ]
            Chú ý: correctIndex là số nguyên từ 0 đến 3 tương ứng vị trí trong mảng options. Toàn bộ bằng tiếng Việt.
        """.trimIndent()

        try {
            val responseText = executePrompt(prompt, apiKey, model)
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedQuestionDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedQuestionDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseQuestionsManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating quiz", e)
            Result.failure(e)
        }
    }

    suspend fun askTutor(
        bookTitle: String,
        bookContext: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cung cấp Gemini API Key để trò chuyện với Gia sư AI."))
        }

        val systemInstruction = GeminiContent(
            parts = listOf(
                GeminiPart(
                    text = """
                        Bạn là Gia Sư AI Chuyên Sâu cho cuốn sách '$bookTitle'.
                        Nhiệm vụ của bạn là giúp người học hiểu thấu đáo, phản biện và ứng dụng kiến thức từ cuốn sách này vào thực tế.
                        
                        NGUYÊN TẮC TRẢ LỜI:
                        1. Luôn dựa trên triết lý và dẫn chứng từ cuốn sách '$bookTitle'.
                        2. Dùng ngôn từ thân thiện, khai sáng, truyền cảm hứng, dễ hiểu.
                        3. Đưa ra ví dụ thực tế hoặc tình huống minh họa sinh động.
                        4. Khuyến khích người học bằng các câu hỏi gợi mở để họ tự chiêm nghiệm.
                        5. Định dạng câu trả lời đẹp mắt với bullet points, in đậm từ khóa quan trọng.
                        
                        Tài liệu trích đoạn từ sách:
                        $bookContext
                    """.trimIndent()
                )
            )
        )

        val contents = mutableListOf<GeminiContent>()

        // Add last 6 turns from history
        val recentHistory = chatHistory.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "model"
            contents.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.content))
                )
            )
        }

        // Add current user message
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        val request = GeminiApiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                maxOutputTokens = 2048
            ),
            systemInstruction = systemInstruction
        )

        try {
            val response = apiService.generateContent(model, apiKey, request)
            val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Không nhận được phản hồi từ AI. Xin vui lòng thử lại."
            Result.success(answer)
        } catch (e: Exception) {
            Log.e(TAG, "Error in tutor chat", e)
            Result.failure(e)
        }
    }

    suspend fun generateStudyPlan(
        bookTitle: String,
        sampleText: String,
        daysCount: Int = 7,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<List<GeneratedStudyDayDto>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cung cấp Gemini API Key để tạo Lộ trình học."))
        }

        val prompt = """
            Thiết kế một Lộ trình học tập $daysCount ngày (Day-by-Day Study Plan) để làm chủ hoàn toàn cuốn sách '$bookTitle'.
            Nội dung sách:
            $sampleText
            
            Mỗi ngày cần có:
            - day: Số nguyên (1 đến $daysCount)
            - title: Tiêu đề chủ đề của ngày
            - goal: Mục tiêu trọng tâm cần đạt được
            - tasks: Danh sách 2-3 hành động cụ thể (đọc, làm bài tập, thực hành thực tế)
            
            Trả về định dạng JSON mảng:
            [
              {
                "day": 1,
                "title": "...",
                "goal": "...",
                "tasks": ["Nhiệm vụ 1", "Nhiệm vụ 2", "Nhiệm vụ 3"]
              }
            ]
            Bằng tiếng Việt.
        """.trimIndent()

        try {
            val responseText = executePrompt(prompt, apiKey, model)
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedStudyDayDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedStudyDayDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseStudyPlanManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating study plan", e)
            Result.failure(e)
        }
    }

    suspend fun explainPageContent(
        bookTitle: String,
        pageNumber: Int,
        pageTextOrSummary: String,
        customKey: String? = null,
        model: String = DEFAULT_MODEL
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Vui lòng cung cấp Gemini API Key."))
        }

        val prompt = """
            Hãy giải thích và phân tích sâu nội dung Trang $pageNumber của cuốn sách '$bookTitle':
            ---
            $pageTextOrSummary
            ---
            
            Yêu cầu:
            1. **Ý chính trang này nói gì?** (2-3 câu tóm gọn)
            2. **Khái niệm hoặc luận điểm quan trọng nhất.**
            3. **Ứng dụng vào thực tế cuộc sống hoặc công việc như thế nào?**
            4. **Một câu hỏi tự chiêm nghiệm dành cho người đọc.**
            
            Định dạng rõ ràng, chuyên nghiệp bằng tiếng Việt.
        """.trimIndent()

        try {
            val answer = executePrompt(prompt, apiKey, model)
            Result.success(answer)
        } catch (e: Exception) {
            Log.e(TAG, "Error explaining page", e)
            Result.failure(e)
        }
    }

    private suspend fun executePrompt(prompt: String, apiKey: String, model: String): String {
        val request = GeminiApiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                maxOutputTokens = 4096
            )
        )
        val response = apiService.generateContent(model, apiKey, request)
        if (response.error != null) {
            throw Exception("Lỗi từ Gemini API (${response.error.code}): ${response.error.message}")
        }
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Không có nội dung trả về từ Gemini API.")
    }

    private fun extractJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.substring(7)
        } else if (text.startsWith("```")) {
            text = text.substring(3)
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length - 3)
        }
        text = text.trim()
        val firstBrace = text.indexOf('{')
        val firstBracket = text.indexOf('[')
        val startIdx = when {
            firstBrace != -1 && firstBracket != -1 -> minOf(firstBrace, firstBracket)
            firstBrace != -1 -> firstBrace
            firstBracket != -1 -> firstBracket
            else -> 0
        }
        val lastBrace = text.lastIndexOf('}')
        val lastBracket = text.lastIndexOf(']')
        val endIdx = when {
            lastBrace != -1 && lastBracket != -1 -> maxOf(lastBrace, lastBracket)
            lastBrace != -1 -> lastBrace
            lastBracket != -1 -> lastBracket
            else -> text.length - 1
        }
        return if (startIdx < endIdx && endIdx < text.length) {
            text.substring(startIdx, endIdx + 1)
        } else {
            text
        }
    }

    private fun parseSummaryManually(jsonStr: String, defaultTitle: String): GeneratedSummaryDto {
        return try {
            val obj = JSONObject(jsonStr)
            val title = obj.optString("title", defaultTitle)
            val author = obj.optString("author", "Tác giả")
            val overview = obj.optString("overview", "")
            val themes = mutableListOf<String>()
            obj.optJSONArray("coreThemes")?.let { arr ->
                for (i in 0 until arr.length()) themes.add(arr.optString(i))
            }
            val takeaways = mutableListOf<String>()
            obj.optJSONArray("keyTakeaways")?.let { arr ->
                for (i in 0 until arr.length()) takeaways.add(arr.optString(i))
            }
            val chapters = mutableListOf<GeneratedChapterDto>()
            obj.optJSONArray("chapters")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val cObj = arr.optJSONObject(i) ?: continue
                    val cPoints = mutableListOf<String>()
                    cObj.optJSONArray("keyPoints")?.let { cpArr ->
                        for (j in 0 until cpArr.length()) cPoints.add(cpArr.optString(j))
                    }
                    chapters.add(
                        GeneratedChapterDto(
                            chapterNumber = cObj.optInt("chapterNumber", i + 1),
                            title = cObj.optString("title", "Chương ${i + 1}"),
                            summary = cObj.optString("summary", ""),
                            keyPoints = cPoints
                        )
                    )
                }
            }
            GeneratedSummaryDto(title, author, overview, themes, takeaways, chapters)
        } catch (e: Exception) {
            GeneratedSummaryDto(defaultTitle, "Tác giả", jsonStr, emptyList(), emptyList(), emptyList())
        }
    }

    private fun parseFlashcardsManually(jsonStr: String): List<GeneratedFlashcardDto> {
        val list = mutableListOf<GeneratedFlashcardDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                list.add(
                    GeneratedFlashcardDto(
                        front = obj.optString("front", ""),
                        back = obj.optString("back", ""),
                        concept = obj.optString("concept", ""),
                        difficulty = obj.optString("difficulty", "Vừa"),
                        chapter = obj.optString("chapter", "Tổng quát")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error manual parse flashcards", e)
        }
        return list
    }

    private fun parseQuestionsManually(jsonStr: String): List<GeneratedQuestionDto> {
        val list = mutableListOf<GeneratedQuestionDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val options = mutableListOf<String>()
                obj.optJSONArray("options")?.let { optArr ->
                    for (j in 0 until optArr.length()) options.add(optArr.optString(j))
                }
                list.add(
                    GeneratedQuestionDto(
                        question = obj.optString("question", ""),
                        options = options,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", ""),
                        reference = obj.optString("reference", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error manual parse questions", e)
        }
        return list
    }

    private fun parseStudyPlanManually(jsonStr: String): List<GeneratedStudyDayDto> {
        val list = mutableListOf<GeneratedStudyDayDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val tasks = mutableListOf<String>()
                obj.optJSONArray("tasks")?.let { tArr ->
                    for (j in 0 until tArr.length()) tasks.add(tArr.optString(j))
                }
                list.add(
                    GeneratedStudyDayDto(
                        day = obj.optInt("day", i + 1),
                        title = obj.optString("title", "Ngày ${i + 1}"),
                        goal = obj.optString("goal", ""),
                        tasks = tasks
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error manual parse study plan", e)
        }
        return list
    }
}
