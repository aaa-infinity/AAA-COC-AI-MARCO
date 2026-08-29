package com.cocai.autoclicker.engine

enum class AiProviderEnum(val displayName: String, val baseUrl: String) {
    GOOGLE_AI_STUDIO("Google AI Studio (Gemini)", "https://generativelanguage.googleapis.com/v1beta"),
    OPENROUTER("OpenRouter Multi-Model", "https://openrouter.ai/api/v1"),
    GROQ("Groq Fast Inference", "https://api.groq.com/openai/v1"),
    CUSTOM_OPENAI("Custom OpenAI Compatible", "https://api.openai.com/v1")
}

data class ApiKeyEntry(
    val id: String,
    val key: String,
    var isRateLimited: Boolean = false,
    var cooldownUntil: Long = 0L
)

data class ProviderProfile(
    val provider: AiProviderEnum,
    val keyPool: MutableList<ApiKeyEntry> = mutableListOf(),
    var selectedModel: String = "gemini-2.0-flash",
    var baseUrl: String = provider.baseUrl
)
