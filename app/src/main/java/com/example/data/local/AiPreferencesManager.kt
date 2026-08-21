package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.AiProvider
import com.example.data.model.AiProviderConfig

class AiPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "pdf_study_ai_prefs"
        private const val KEY_ACTIVE_PROVIDER = "active_ai_provider"
        private const val KEY_PREFIX_API_KEY = "api_key_"
        private const val KEY_PREFIX_MODEL = "model_"
        private const val KEY_PREFIX_ENDPOINT = "endpoint_"
    }

    fun getActiveProvider(): AiProvider {
        val id = prefs.getString(KEY_ACTIVE_PROVIDER, AiProvider.GEMINI.id)
        return AiProvider.fromId(id)
    }

    fun setActiveProvider(provider: AiProvider) {
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, provider.id).apply()
    }

    fun getApiKeyForProvider(provider: AiProvider): String {
        val savedKey = prefs.getString(KEY_PREFIX_API_KEY + provider.id, "") ?: ""
        if (savedKey.isNotBlank()) return savedKey.trim()

        // Fallback for Gemini if empty
        if (provider == AiProvider.GEMINI) {
            return try {
                val configKey = BuildConfig.GEMINI_API_KEY
                if (configKey != "MY_GEMINI_API_KEY") configKey else ""
            } catch (e: Exception) {
                ""
            }
        }
        return ""
    }

    fun saveApiKeyForProvider(provider: AiProvider, apiKey: String) {
        prefs.edit().putString(KEY_PREFIX_API_KEY + provider.id, apiKey.trim()).apply()
    }

    fun getModelForProvider(provider: AiProvider): String {
        return prefs.getString(KEY_PREFIX_MODEL + provider.id, provider.defaultModel) ?: provider.defaultModel
    }

    fun saveModelForProvider(provider: AiProvider, model: String) {
        val m = if (model.isBlank()) provider.defaultModel else model.trim()
        prefs.edit().putString(KEY_PREFIX_MODEL + provider.id, m).apply()
    }

    fun getEndpointForProvider(provider: AiProvider): String {
        return prefs.getString(KEY_PREFIX_ENDPOINT + provider.id, provider.defaultEndpoint) ?: provider.defaultEndpoint
    }

    fun saveEndpointForProvider(provider: AiProvider, endpoint: String) {
        val ep = if (endpoint.isBlank()) provider.defaultEndpoint else endpoint.trim()
        prefs.edit().putString(KEY_PREFIX_ENDPOINT + provider.id, ep).apply()
    }

    fun getActiveConfig(): AiProviderConfig {
        val provider = getActiveProvider()
        return AiProviderConfig(
            provider = provider,
            apiKey = getApiKeyForProvider(provider),
            model = getModelForProvider(provider),
            customEndpoint = getEndpointForProvider(provider)
        )
    }

    fun saveActiveConfig(config: AiProviderConfig) {
        setActiveProvider(config.provider)
        saveApiKeyForProvider(config.provider, config.apiKey)
        saveModelForProvider(config.provider, config.model)
        saveEndpointForProvider(config.provider, config.customEndpoint)
    }
}
