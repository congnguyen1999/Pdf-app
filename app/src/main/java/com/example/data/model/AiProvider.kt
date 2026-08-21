package com.example.data.model

import com.squareup.moshi.JsonClass

enum class AiProvider(
    val id: String,
    val displayName: String,
    val shortName: String,
    val defaultEndpoint: String,
    val defaultModel: String,
    val presetModels: List<String>,
    val requiresCustomEndpoint: Boolean = false,
    val keyHint: String = "Nhập API Key...",
    val description: String = "",
    val badgeColorHex: String = "#6750A4"
) {
    VILAO(
        id = "vilao",
        displayName = "Vilao AI",
        shortName = "Vilao",
        defaultEndpoint = "https://api.vilao.ai/v1",
        defaultModel = "gpt-4o-mini",
        presetModels = listOf("gpt-4o-mini", "gpt-4o", "deepseek-chat", "deepseek-reasoner", "claude-3-5-sonnet", "gemini-2.5-flash"),
        requiresCustomEndpoint = false,
        keyHint = "Nhập Vilao API Key...",
        description = "Cổng kết nối AI đa mô hình qua API Vilao (https://api.vilao.ai/v1).",
        badgeColorHex = "#2563EB"
    ),
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        shortName = "Gemini",
        defaultEndpoint = "https://generativelanguage.googleapis.com/",
        defaultModel = "gemini-3.5-flash",
        presetModels = listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-3.1-pro-preview"),
        requiresCustomEndpoint = false,
        keyHint = "AIzaSy... (hoặc để trống dùng key hệ thống)",
        description = "Mô hình đa phương thức thế hệ mới của Google, hỗ trợ đọc PDF & tài liệu siêu tốc.",
        badgeColorHex = "#4F46E5"
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI (ChatGPT)",
        shortName = "OpenAI",
        defaultEndpoint = "https://api.openai.com/v1/",
        defaultModel = "gpt-4o-mini",
        presetModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4-turbo", "gpt-3.5-turbo", "o1-mini"),
        requiresCustomEndpoint = false,
        keyHint = "sk-proj-...",
        description = "Mô hình GPT-4o và GPT-4o-mini từ OpenAI, tóm tắt và phản biện xuất sắc.",
        badgeColorHex = "#10A37F"
    ),
    DEEPSEEK(
        id = "deepseek",
        displayName = "DeepSeek AI",
        shortName = "DeepSeek",
        defaultEndpoint = "https://api.deepseek.com/v1/",
        defaultModel = "deepseek-chat",
        presetModels = listOf("deepseek-chat", "deepseek-reasoner"),
        requiresCustomEndpoint = false,
        keyHint = "sk-...",
        description = "Mô hình DeepSeek V3 và R1 (Reasoner) mạnh mẽ về tư duy logic và chi phí tối ưu.",
        badgeColorHex = "#0284C7"
    ),
    CLAUDE(
        id = "claude",
        displayName = "Anthropic Claude",
        shortName = "Claude",
        defaultEndpoint = "https://api.anthropic.com/v1/",
        defaultModel = "claude-3-5-sonnet-20241022",
        presetModels = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229"),
        requiresCustomEndpoint = false,
        keyHint = "sk-ant-api03-...",
        description = "Mô hình Claude 3.5 Sonnet / Haiku của Anthropic, phân tích tài liệu văn phong tự nhiên.",
        badgeColorHex = "#D97706"
    ),
    GROQ(
        id = "groq",
        displayName = "Groq (LPU Speed)",
        shortName = "Groq",
        defaultEndpoint = "https://api.groq.com/openai/v1/",
        defaultModel = "llama-3.3-70b-versatile",
        presetModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768"),
        requiresCustomEndpoint = false,
        keyHint = "gsk_...",
        description = "Tốc độ xử lý siêu nhanh dựa trên kiến trúc LPU phần cứng chuyên biệt của Groq.",
        badgeColorHex = "#F97316"
    ),
    CUSTOM(
        id = "custom",
        displayName = "Tùy Chọn / OpenAI-Compatible",
        shortName = "Tùy Chỉnh",
        defaultEndpoint = "https://api.vilao.ai/v1",
        defaultModel = "gpt-4o-mini",
        presetModels = listOf("gpt-4o-mini", "gpt-4o", "deepseek-chat", "deepseek-reasoner", "claude-3-5-sonnet", "google/gemini-2.0-flash-exp:free"),
        requiresCustomEndpoint = true,
        keyHint = "Khóa API...",
        description = "Tự do kết nối máy chủ OpenAI-Compatible, Vilao AI, Ollama (Local), LM Studio, vLLM hoặc OpenRouter.",
        badgeColorHex = "#7C3AED"
    );

    companion object {
        fun fromId(id: String?): AiProvider {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
        }
    }
}

@JsonClass(generateAdapter = true)
data class AiProviderConfig(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val model: String = AiProvider.GEMINI.defaultModel,
    val customEndpoint: String = AiProvider.GEMINI.defaultEndpoint,
    val temperature: Float = 0.5f
)
