package com.example.trabalhopradalogin.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trabalhopradalogin.ui.screens.LoginScreen
import com.example.trabalhopradalogin.ui.screens.RegisterScreen
import com.example.trabalhopradalogin.ui.screens.ForgotPasswordScreen
import androidx.compose.ui.platform.LocalContext
import com.example.trabalhopradalogin.data.AppDatabase
import com.example.trabalhopradalogin.ui.screens.MenuScreen
import com.example.trabalhopradalogin.viewmodel.AuthViewModel
import com.example.trabalhopradalogin.viewmodel.AuthViewModelFactory
import com.example.trabalhopradalogin.viewmodel.TripViewModel
import com.example.trabalhopradalogin.viewmodel.TripViewModelFactory
import com.example.trabalhopradalogin.ui.screens.NewTripScreen
import com.example.trabalhopradalogin.ui.screens.MyTripsScreen
import com.example.trabalhopradalogin.ui.screens.AboutScreen
import com.example.trabalhopradalogin.ui.screens.TripPhotosScreen
import com.example.trabalhopradalogin.viewmodel.TripPhotoViewModel
import androidx.compose.runtime.remember
import com.example.trabalhopradalogin.data.repository.ItineraryRepository
import com.example.trabalhopradalogin.viewmodel.ItineraryViewModel
import com.example.trabalhopradalogin.viewmodel.ItineraryViewModelFactory
import com.example.trabalhopradalogin.ui.screens.ItineraryScreen

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val userDao = database.userDao()
    val tripDao = database.tripDao()
    val navController = rememberNavController()
    
    // Instanciamos o ViewModel usando a Factory para passar o UserDao
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userDao)
    )

    val tripViewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(tripDao)
    )

    val tripPhotoViewModel: TripPhotoViewModel = viewModel()

    val itineraryRepository = remember { ItineraryRepository() }
    val itineraryViewModel: ItineraryViewModel = viewModel(
        factory = ItineraryViewModelFactory(itineraryRepository, tripDao)
    )

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
            MenuScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("new_trip") {
            NewTripScreen(navController = navController, tripViewModel = tripViewModel, authViewModel = authViewModel)
        }
        composable("my_trips") {
            MyTripsScreen(navController = navController, tripViewModel = tripViewModel, authViewModel = authViewModel)
        }
        composable("about") {
            AboutScreen(navController = navController)
        }
        composable("trip_photos/{tripId}") { backStackEntry ->
            val tripIdStr = backStackEntry.arguments?.getString("tripId")
            val tripId = tripIdStr?.toIntOrNull() ?: 0
            TripPhotosScreen(navController = navController, tripId = tripId, viewModel = tripPhotoViewModel)
        }
        composable("generate_itinerary") {
            ItineraryScreen(navController = navController, viewModel = itineraryViewModel, authViewModel = authViewModel, tripId = null)
        }
        composable("generate_itinerary/{tripId}") { backStackEntry ->
            val tripIdStr = backStackEntry.arguments?.getString("tripId")
            val tripId = tripIdStr?.toIntOrNull() ?: 0
            ItineraryScreen(navController = navController, viewModel = itineraryViewModel, authViewModel = authViewModel, tripId = tripId)
        }
    }
}
