package com.example.trabalhopradalogin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trabalhopradalogin.viewmodel.AuthViewModel

@Composable
fun MenuScreen(navController: NavController, viewModel: AuthViewModel) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Painel Principal (Menu)", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Bem-vindo ao Gerenciador de Viagens!")
            Text("Esta é uma tela placeholder de entrada pós-login.")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    // Simula logout e volta para tela de origem limpa.
                    navController.navigate("login") {
                        popUpTo("menu") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Deslogar / Sair")
            }
        }
    }
}
