package com.example.trabalhopradalogin.viewmodel

import androidx.lifecycle.ViewModel

// ViewModel bem simples para manter o estado da lógica, como validações e credenciais
class AuthViewModel : ViewModel() {
    // Aqui você poderia armazenar os estados reais de autenticação futuramente
    // val isUserLoggedIn = mutableStateOf(false)
    
    // Funções simuladas para validação da regra de negócio
    fun resetPassword(email: String): Boolean {
        // Simulação de sucesso no reset - verifica se tem o formato mínimo
        return email.isNotBlank() && email.contains("@")
    }
}
