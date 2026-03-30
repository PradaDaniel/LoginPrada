package com.example.trabalhopradalogin.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trabalhopradalogin.ui.screens.LoginScreen
import com.example.trabalhopradalogin.ui.screens.RegisterScreen
import com.example.trabalhopradalogin.ui.screens.ForgotPasswordScreen
import com.example.trabalhopradalogin.ui.screens.MenuScreen
import com.example.trabalhopradalogin.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instanciamos o ViewModel que será compartilhado ou fornecido via DI em um projeto maior
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController, viewModel = authViewModel)
        }
        composable("menu") {
            MenuScreen(navController = navController, viewModel = authViewModel)
        }
    }
}
