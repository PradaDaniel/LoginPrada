 package com.example.trabalhopradalogin.data.model

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: CandidateContent?,
    val finishReason: String?
)

data class CandidateContent(
    val parts: List<PartResponse>?,
    val role: String?
)

data class PartResponse(
    val text: String?
)
