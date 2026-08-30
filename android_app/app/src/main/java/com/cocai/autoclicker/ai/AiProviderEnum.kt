package com.cocai.autoclicker.ai

enum class AiProviderEnum(val displayName: String, val baseUrl: String) {
    GOOGLE_AI_STUDIO("Google Gemini (Official)", "https://generativelanguage.googleapis.com/v1beta"),
    OPENROUTER("OpenRouter Multi-Model Vision", "https://openrouter.ai/api/v1"),
    GROQ("Groq Fast Vision Inference", "https://api.groq.com/openai/v1"),
    CUSTOM_OPENAI("DeepSeek / OpenAI Compatible", "https://api.deepseek.com/v1")
}
