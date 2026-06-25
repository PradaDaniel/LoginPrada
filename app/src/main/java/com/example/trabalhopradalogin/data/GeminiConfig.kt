package com.example.trabalhopradalogin.data

object GeminiConfig {
    /**
     * ATENÇÃO / AVISO DE SEGURANÇA:
     * A chave de API está hardcoded aqui apenas para fins didáticos, de teste e entrega rápida do projeto.
     * Em um ambiente de produção real, NUNCA exponha chaves de API diretamente no código-fonte.
     * 
     * Práticas recomendadas para Produção:
     * 1. Obter a chave dinamicamente a partir de um servidor back-end seguro.
     * 2. Configurar a chave no arquivo `local.properties` (que é ignorado pelo Git) e injetá-la via BuildConfig.
     * 3. Utilizar ferramentas como Google Cloud Secret Manager.
     */
    // Insira a chave de API do Gemini abaixo (Ex: "AQ.Ab8RN6...")
    const val API_KEY = ""
    const val BASE_URL = "https://generativelanguage.googleapis.com/"
}
