package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiProvider
import com.example.data.model.AiProviderConfig
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeneratedChapterDto
import com.example.data.model.GeneratedFlashcardDto
import com.example.data.model.GeneratedQuestionDto
import com.example.data.model.GeneratedStudyDayDto
import com.example.data.model.GeneratedSummaryDto
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class UniversalAiService(
    private val geminiStudyService: GeminiStudyService = GeminiStudyService()
) {
    companion object {
        private const val TAG = "UniversalAiService"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateBookOverview(
        bookTitle: String,
        sampleText: String,
        config: AiProviderConfig
    ): Result<GeneratedSummaryDto> = withContext(Dispatchers.IO) {
        val prompt = """
            Bạn là một chuyên gia giáo dục, học giả và nhà phân tích sách hàng đầu thế giới.
            Dưới đây là toàn bộ văn bản được trích xuất từng trang của cuốn sách '$bookTitle'.
            YÊU CẦU QUAN TRỌNG: Bạn PHẢI đọc kỹ lưỡng toàn bộ văn bản của tất cả các trang từ đầu đến cuối, KHÔNG được bỏ sót bất kỳ trang nào, không bỏ qua nội dung chi tiết.
            
            NỘI DUNG TỪNG TRANG:
            ---
            $sampleText
            ---
            
            Hãy phân tích toàn diện và trả về kết quả định dạng JSON thuần túy (không kèm markdown ```json bọc ngoài nếu có thể, hoặc bọc trong ```json) theo cấu trúc chính xác sau:
            {
              "title": "$bookTitle",
              "author": "Tên tác giả (hoặc tác giả được ghi trong tài liệu)",
              "overview": "Tóm tắt tổng quan chuyên sâu, bao quát toàn bộ nội dung từ các trang (3-4 đoạn văn)",
              "coreThemes": ["Chủ đề chính 1", "Chủ đề chính 2", "Chủ đề chính 3", "Chủ đề chính 4"],
              "keyTakeaways": ["Bài học thực tiễn 1", "Bài học thực tiễn 2", "Bài học thực tiễn 3", "Bài học thực tiễn 4", "Bài học thực tiễn 5"],
              "chapters": [
                {
                  "chapterNumber": 1,
                  "title": "Tên chương 1",
                  "summary": "Tóm tắt chi tiết nội dung cốt lõi của chương 1",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                },
                {
                  "chapterNumber": 2,
                  "title": "Tên chương 2",
                  "summary": "Tóm tắt chi tiết nội dung cốt lõi của chương 2",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                },
                {
                  "chapterNumber": 3,
                  "title": "Tên chương 3",
                  "summary": "Tóm tắt chi tiết nội dung cốt lõi của chương 3",
                  "keyPoints": ["Điểm cốt lõi 1", "Điểm cốt lõi 2"]
                }
              ]
            }
            Chú ý: Toàn bộ nội dung trả về bằng tiếng Việt rõ ràng, sâu sắc, chuẩn xác, trung thực với nội dung các trang đã đọc.
        """.trimIndent()

        try {
            val responseText = executePromptUniversal(
                systemInstruction = "Bạn là chuyên gia phân tích và tóm tắt sách chuyên sâu. Bạn đọc hết tất cả các trang của tài liệu tải lên thật kỹ lưỡng, không bỏ sót trang nào, đảm bảo tính chuẩn xác và không sai sót. Luôn trả về định dạng JSON hợp lệ.",
                prompt = prompt,
                config = config
            )
            val cleanJson = extractJsonString(responseText)
            val adapter = GeminiApiClient.moshi.adapter(GeneratedSummaryDto::class.java)
            val result = adapter.fromJson(cleanJson) ?: parseSummaryManually(cleanJson, bookTitle)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateBookOverview with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun generateFlashcards(
        bookTitle: String,
        sampleText: String,
        count: Int = 6,
        config: AiProviderConfig
    ): Result<List<GeneratedFlashcardDto>> = withContext(Dispatchers.IO) {
        val prompt = """
            Tạo $count thẻ ghi nhớ (Flashcards) chất lượng cao để ôn tập cuốn sách '$bookTitle'.
            Dựa trên toàn bộ nội dung chi tiết các trang trong sách dưới đây (đọc kỹ không bỏ trang):
            $sampleText
            
            Mỗi thẻ cần có:
            - front: Câu hỏi gợi nhớ sâu hoặc khái niệm cần định nghĩa từ nội dung thực tế trong sách
            - back: Lời giải thích súc tích, trực diện, chính xác theo sách kèm ví dụ
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
            Bằng tiếng Việt chuẩn xác.
        """.trimIndent()

        try {
            val responseText = executePromptUniversal(
                systemInstruction = "Bạn là chuyên gia thiết kế Flashcards học tập. Bạn đọc toàn bộ các trang tài liệu thật kỹ, không bỏ sót trang nào, trích xuất chính xác khái niệm từ sách.",
                prompt = prompt,
                config = config
            )
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedFlashcardDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedFlashcardDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseFlashcardsManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateFlashcards with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun generateQuiz(
        bookTitle: String,
        sampleText: String,
        count: Int = 5,
        config: AiProviderConfig
    ): Result<List<GeneratedQuestionDto>> = withContext(Dispatchers.IO) {
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
            val responseText = executePromptUniversal(
                systemInstruction = "Bạn là chuyên gia soạn thảo đề thi đánh giá năng lực tư duy. Luôn trả về JSON mảng hợp lệ.",
                prompt = prompt,
                config = config
            )
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedQuestionDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedQuestionDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseQuestionsManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateQuiz with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun askTutor(
        bookTitle: String,
        bookContext: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        val systemPrompt = """
            Bạn là Gia Sư AI Chuyên Sâu cho cuốn sách '$bookTitle' (${config.provider.displayName} - ${config.model}).
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

        try {
            when (config.provider) {
                AiProvider.GEMINI -> {
                    val resolvedKey = resolveApiKey(config.apiKey, config.provider)
                    if (resolvedKey.isBlank()) {
                        return@withContext Result.failure(Exception("Vui lòng cấu hình Gemini API Key."))
                    }
                    geminiStudyService.askTutor(
                        bookTitle = bookTitle,
                        bookContext = bookContext,
                        chatHistory = chatHistory,
                        userMessage = userMessage,
                        customKey = resolvedKey,
                        model = config.model
                    )
                }
                AiProvider.OPENAI,
                AiProvider.DEEPSEEK,
                AiProvider.GROQ,
                AiProvider.VILAO,
                AiProvider.CUSTOM -> {
                    executeOpenAiChat(
                        systemPrompt = systemPrompt,
                        chatHistory = chatHistory,
                        userMessage = userMessage,
                        config = config
                    )
                }
                AiProvider.CLAUDE -> {
                    executeClaudeChat(
                        systemPrompt = systemPrompt,
                        chatHistory = chatHistory,
                        userMessage = userMessage,
                        config = config
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in askTutor with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun generateStudyPlan(
        bookTitle: String,
        sampleText: String,
        daysCount: Int = 7,
        config: AiProviderConfig
    ): Result<List<GeneratedStudyDayDto>> = withContext(Dispatchers.IO) {
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
            val responseText = executePromptUniversal(
                systemInstruction = "Bạn là chuyên gia lập kế hoạch học tập khoa học. Luôn trả về JSON mảng hợp lệ.",
                prompt = prompt,
                config = config
            )
            val cleanJson = extractJsonString(responseText)
            val listType = Types.newParameterizedType(List::class.java, GeneratedStudyDayDto::class.java)
            val adapter = GeminiApiClient.moshi.adapter<List<GeneratedStudyDayDto>>(listType)
            val result = adapter.fromJson(cleanJson) ?: parseStudyPlanManually(cleanJson)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateStudyPlan with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun explainPageContent(
        bookTitle: String,
        pageNumber: Int,
        pageTextOrSummary: String,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
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
            val answer = executePromptUniversal(
                systemInstruction = "Bạn là chuyên gia diễn giải và phân tích tài liệu sách giáo dục.",
                prompt = prompt,
                config = config
            )
            Result.success(answer)
        } catch (e: Exception) {
            Log.e(TAG, "Error in explainPageContent with ${config.provider.displayName}", e)
            Result.failure(e)
        }
    }

    suspend fun testAiConnection(config: AiProviderConfig): Result<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val prompt = "Hãy trả lời đúng 1 câu ngắn chào mừng và xác nhận bạn là model ${config.model}."
        try {
            val response = executePromptUniversal(
                systemInstruction = "Bạn là trợ lý AI thông minh.",
                prompt = prompt,
                config = config
            )
            val duration = System.currentTimeMillis() - startTime
            Result.success("Kết nối thành công (${duration}ms):\n$response")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Universal Prompt Execution Routing
    private suspend fun executePromptUniversal(
        systemInstruction: String,
        prompt: String,
        config: AiProviderConfig
    ): String {
        return when (config.provider) {
            AiProvider.GEMINI -> {
                val resolvedKey = resolveApiKey(config.apiKey, config.provider)
                if (resolvedKey.isBlank() || resolvedKey == "MY_GEMINI_API_KEY") {
                    throw Exception("Vui lòng nhập GEMINI_API_KEY trong phần Cài đặt AI.")
                }
                // Call Gemini REST API directly or via geminiStudyService
                executeGeminiPromptDirect(systemInstruction, prompt, resolvedKey, config.model)
            }
            AiProvider.OPENAI,
            AiProvider.DEEPSEEK,
            AiProvider.GROQ,
            AiProvider.VILAO,
            AiProvider.CUSTOM -> {
                val resolvedKey = resolveApiKey(config.apiKey, config.provider)
                if (resolvedKey.isBlank() && config.provider != AiProvider.CUSTOM) {
                    throw Exception("Vui lòng nhập API Key cho ${config.provider.displayName}.")
                }
                executeOpenAiCompatiblePrompt(systemInstruction, prompt, resolvedKey, config)
            }
            AiProvider.CLAUDE -> {
                val resolvedKey = resolveApiKey(config.apiKey, config.provider)
                if (resolvedKey.isBlank()) {
                    throw Exception("Vui lòng nhập Claude API Key (sk-ant-...).")
                }
                executeClaudePrompt(systemInstruction, prompt, resolvedKey, config)
            }
        }
    }

    private fun resolveApiKey(customKey: String?, provider: AiProvider): String {
        val trimmed = customKey?.trim() ?: ""
        if (trimmed.isNotBlank()) return trimmed

        if (provider == AiProvider.GEMINI) {
            return try {
                val defaultKey = BuildConfig.GEMINI_API_KEY
                if (defaultKey != "MY_GEMINI_API_KEY") defaultKey else ""
            } catch (e: Exception) {
                ""
            }
        }
        return ""
    }

    private suspend fun executeGeminiPromptDirect(
        systemInstruction: String,
        prompt: String,
        apiKey: String,
        model: String
    ): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootObj = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 4096)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(rootObj.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMsg = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Lỗi Gemini API [${response.code}]: $errMsg")
        }

        val jsonRes = JSONObject(responseBody)
        val candidates = jsonRes.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }
        throw Exception("Không nhận được nội dung phản hồi từ Gemini.")
    }

    private suspend fun executeOpenAiCompatiblePrompt(
        systemInstruction: String,
        prompt: String,
        apiKey: String,
        config: AiProviderConfig
    ): String = withContext(Dispatchers.IO) {
        val baseUrl = config.customEndpoint.trim().let {
            if (it.endsWith("/")) it else "$it/"
        }
        val url = if (baseUrl.endsWith("chat/completions/")) {
            baseUrl.removeSuffix("/")
        } else if (baseUrl.endsWith("chat/completions")) {
            baseUrl
        } else {
            "${baseUrl}chat/completions"
        }

        val rootObj = JSONObject().apply {
            put("model", config.model)
            val messagesArr = JSONArray()
            if (systemInstruction.isNotBlank()) {
                messagesArr.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
            }
            messagesArr.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
            put("messages", messagesArr)
            put("temperature", 0.3)
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .post(rootObj.toString().toRequestBody(JSON_MEDIA_TYPE))

        if (apiKey.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val response = httpClient.newCall(reqBuilder.build()).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMsg = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Lỗi ${config.provider.displayName} API [${response.code}]: $errMsg")
        }

        val jsonRes = JSONObject(responseBody)
        val choices = jsonRes.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val msg = choices.getJSONObject(0).optJSONObject("message")
            val content = msg?.optString("content")
            if (!content.isNullOrBlank()) {
                return@withContext content
            }
        }
        throw Exception("Không nhận được phản hồi hợp lệ từ ${config.provider.displayName}.")
    }

    private suspend fun executeOpenAiChat(
        systemPrompt: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(config.apiKey, config.provider)
        val baseUrl = config.customEndpoint.trim().let {
            if (it.endsWith("/")) it else "$it/"
        }
        val url = if (baseUrl.endsWith("chat/completions/")) {
            baseUrl.removeSuffix("/")
        } else if (baseUrl.endsWith("chat/completions")) {
            baseUrl
        } else {
            "${baseUrl}chat/completions"
        }

        val rootObj = JSONObject().apply {
            put("model", config.model)
            val messagesArr = JSONArray()
            messagesArr.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            // Add last 6 turns
            val recent = chatHistory.takeLast(6)
            for (msg in recent) {
                val role = if (msg.sender == "user") "user" else "assistant"
                messagesArr.put(JSONObject().apply {
                    put("role", role)
                    put("content", msg.content)
                })
            }
            // Add current message
            messagesArr.put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
            put("messages", messagesArr)
            put("temperature", 0.7)
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .post(rootObj.toString().toRequestBody(JSON_MEDIA_TYPE))

        if (apiKey.isNotBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val response = httpClient.newCall(reqBuilder.build()).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMsg = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            return@withContext Result.failure(Exception("Lỗi ${config.provider.displayName} [${response.code}]: $errMsg"))
        }

        val jsonRes = JSONObject(responseBody)
        val choices = jsonRes.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val msg = choices.getJSONObject(0).optJSONObject("message")
            val content = msg?.optString("content")
            if (!content.isNullOrBlank()) {
                return@withContext Result.success(content)
            }
        }
        Result.failure(Exception("Không nhận được phản hồi từ ${config.provider.displayName}."))
    }

    private suspend fun executeClaudePrompt(
        systemInstruction: String,
        prompt: String,
        apiKey: String,
        config: AiProviderConfig
    ): String = withContext(Dispatchers.IO) {
        val url = "https://api.anthropic.com/v1/messages"

        val rootObj = JSONObject().apply {
            put("model", config.model)
            put("max_tokens", 4096)
            if (systemInstruction.isNotBlank()) {
                put("system", systemInstruction)
            }
            val messagesArr = JSONArray()
            messagesArr.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
            put("messages", messagesArr)
            put("temperature", 0.3)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(rootObj.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMsg = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Lỗi Claude API [${response.code}]: $errMsg")
        }

        val jsonRes = JSONObject(responseBody)
        val contentArr = jsonRes.optJSONArray("content")
        if (contentArr != null && contentArr.length() > 0) {
            val text = contentArr.getJSONObject(0).optString("text")
            if (!text.isNullOrBlank()) {
                return@withContext text
            }
        }
        throw Exception("Không nhận được phản hồi từ Claude.")
    }

    private suspend fun executeClaudeChat(
        systemPrompt: String,
        chatHistory: List<ChatMessageEntity>,
        userMessage: String,
        config: AiProviderConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(config.apiKey, config.provider)
        val url = "https://api.anthropic.com/v1/messages"

        val rootObj = JSONObject().apply {
            put("model", config.model)
            put("max_tokens", 2048)
            put("system", systemPrompt)
            val messagesArr = JSONArray()
            val recent = chatHistory.takeLast(6)
            for (msg in recent) {
                val role = if (msg.sender == "user") "user" else "assistant"
                messagesArr.put(JSONObject().apply {
                    put("role", role)
                    put("content", msg.content)
                })
            }
            messagesArr.put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })
            put("messages", messagesArr)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(rootObj.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errMsg = try {
                JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            return@withContext Result.failure(Exception("Lỗi Claude API [${response.code}]: $errMsg"))
        }

        val jsonRes = JSONObject(responseBody)
        val contentArr = jsonRes.optJSONArray("content")
        if (contentArr != null && contentArr.length() > 0) {
            val text = contentArr.getJSONObject(0).optString("text")
            if (!text.isNullOrBlank()) {
                return@withContext Result.success(text)
            }
        }
        Result.failure(Exception("Không nhận được phản hồi từ Claude."))
    }

    // JSON String Extraction and Parsing Helpers
    private fun extractJsonString(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json").trim()
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```").trim()
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```").trim()
        }

        val firstObj = text.indexOf('{')
        val lastObj = text.lastIndexOf('}')
        val firstArr = text.indexOf('[')
        val lastArr = text.lastIndexOf(']')

        return if (firstArr != -1 && (firstObj == -1 || firstArr < firstObj) && lastArr > firstArr) {
            text.substring(firstArr, lastArr + 1)
        } else if (firstObj != -1 && lastObj > firstObj) {
            text.substring(firstObj, lastObj + 1)
        } else {
            text
        }
    }

    private fun parseSummaryManually(jsonStr: String, defaultTitle: String): GeneratedSummaryDto {
        return try {
            val json = JSONObject(jsonStr)
            val title = json.optString("title", defaultTitle)
            val author = json.optString("author", "Tác giả")
            val overview = json.optString("overview", "")
            val themes = json.optJSONArray("coreThemes")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()
            val takeaways = json.optJSONArray("keyTakeaways")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()

            val chapterList = mutableListOf<GeneratedChapterDto>()
            val chapArr = json.optJSONArray("chapters")
            if (chapArr != null) {
                for (i in 0 until chapArr.length()) {
                    val cObj = chapArr.getJSONObject(i)
                    val kPoints = cObj.optJSONArray("keyPoints")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList()
                    chapterList.add(
                        GeneratedChapterDto(
                            chapterNumber = cObj.optInt("chapterNumber", i + 1),
                            title = cObj.optString("title", "Chương ${i + 1}"),
                            summary = cObj.optString("summary", ""),
                            keyPoints = kPoints
                        )
                    )
                }
            }
            GeneratedSummaryDto(title, author, overview, themes, takeaways, chapterList)
        } catch (e: Exception) {
            GeneratedSummaryDto(title = defaultTitle, overview = jsonStr)
        }
    }

    private fun parseFlashcardsManually(jsonStr: String): List<GeneratedFlashcardDto> {
        val list = mutableListOf<GeneratedFlashcardDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
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
            Log.e(TAG, "Failed manual flashcard parse", e)
        }
        return list
    }

    private fun parseQuestionsManually(jsonStr: String): List<GeneratedQuestionDto> {
        val list = mutableListOf<GeneratedQuestionDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optArr = obj.optJSONArray("options")
                val opts = if (optArr != null) {
                    (0 until optArr.length()).map { optArr.getString(it) }
                } else emptyList()

                list.add(
                    GeneratedQuestionDto(
                        question = obj.optString("question", ""),
                        options = opts,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", ""),
                        reference = obj.optString("reference", "")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed manual quiz parse", e)
        }
        return list
    }

    private fun parseStudyPlanManually(jsonStr: String): List<GeneratedStudyDayDto> {
        val list = mutableListOf<GeneratedStudyDayDto>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val taskArr = obj.optJSONArray("tasks")
                val tasks = if (taskArr != null) {
                    (0 until taskArr.length()).map { taskArr.getString(it) }
                } else emptyList()

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
            Log.e(TAG, "Failed manual study plan parse", e)
        }
        return list
    }
}
