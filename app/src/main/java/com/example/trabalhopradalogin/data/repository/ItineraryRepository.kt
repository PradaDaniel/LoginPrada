package com.example.trabalhopradalogin.data.repository

import com.example.trabalhopradalogin.data.GeminiConfig
import com.example.trabalhopradalogin.data.api.GeminiApiService
import com.example.trabalhopradalogin.data.model.Content
import com.example.trabalhopradalogin.data.model.GeminiRequest
import com.example.trabalhopradalogin.data.model.Part
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ItineraryRepository {

    private val apiService: GeminiApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(GeminiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateItinerary(
        destination: String,
        duration: String,
        interests: String,
        style: String,
        budget: String
    ): Result<String> {
        return try {
            val prompt = """
                Crie um roteiro turístico detalhado e personalizado para uma viagem.
                Detalhes da viagem:
                - Destino: $destination
                - Duração/Período: $duration
                - Interesses: $interests
                - Estilo de Viagem: $style
                - Orçamento: $budget

                Por favor, formate a resposta com títulos claros para cada dia, use tópicos e seja específico nas atrações recomendadas, opções de restaurantes e dicas locais de viagem. Destaque pontos importantes em negrito. Escreva a resposta em português brasileiro.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                )
            )

            val response = apiService.generateContent(
                apiKey = GeminiConfig.API_KEY,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Nenhum conteúdo retornado pelo Gemini."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
