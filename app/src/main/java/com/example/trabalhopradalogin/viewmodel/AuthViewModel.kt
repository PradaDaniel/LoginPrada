package com.example.trabalhopradalogin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.trabalhopradalogin.data.User
import com.example.trabalhopradalogin.data.UserDao
import kotlinx.coroutines.launch

// ViewModel para manter o estado da lógica, como validações e credenciais
class AuthViewModel(private val userDao: UserDao) : ViewModel() {
    
    // Função para registrar o usuário no banco de dados Room
    fun registerUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userDao.insertUser(user)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    
    var loggedInUser: User? = null
        private set

    // Função para verificar se o usuário existe para o Login
    fun loginUser(email: String, senha: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.senha == senha) {
                loggedInUser = user
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }


    // Funções simuladas para validação da regra de negócio
    fun resetPassword(email: String): Boolean {
        // Simulação de sucesso no reset - verifica se tem o formato mínimo
        return email.isNotBlank() && email.contains("@")
    }
}
