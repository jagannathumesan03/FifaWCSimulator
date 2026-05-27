package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Serializable Data Classes for Gemini REST API ---

data class Part(
    @Json(name = "text") val text: String? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

data class Candidate(
    @Json(name = "content") val content: Content? = null
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

// --- Retrofit Service Definitions ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApi by lazy {
        retrofit.create(GeminiApi::class.java)
    }

    /**
     * Calls Gemini 3.5 Flash to generate professional sports analytics commentary or bracket feedback.
     */
    suspend fun fetchAnalysis(prompt: String, systemPrompt: String = "You are a professional football analyst reporting on the FIFA World Cup 2026. Keep analyses concise, engaging, and rich with tactical facts."): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Note: Gemini API Key is missing. Create the key in the AI Studio Secrets panel to activate live AI predictions! (Fallback analysis active: This matchup combines explosive wing play with compact midfields)."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text ?: "Could not extract analytical commentary. Try re-simulating the fixture."
        } catch (e: Exception) {
            "Analysis unavailable (Offline). ${e.localizedMessage ?: "No connection."}"
        }
    }
}
