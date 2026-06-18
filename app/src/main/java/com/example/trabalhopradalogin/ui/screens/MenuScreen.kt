package com.example.trabalhopradalogin.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trabalhopradalogin.data.Trip
import com.example.trabalhopradalogin.viewmodel.AuthViewModel
import com.example.trabalhopradalogin.viewmodel.LocationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController, authViewModel: AuthViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as Activity
    
    val locationViewModel: LocationViewModel = viewModel()
    val locationState by locationViewModel.state
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            authViewModel.loggedInUser?.id?.let { locationViewModel.fetchLocationAndTrip(it) }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Fechar app ao voltar se estiver no menu
    BackHandler {
        context.finish()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Prada Viagens",
                    modifier = Modifier.padding(horizontal = 28.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Nova viagem") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("new_trip")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Minhas Viagens") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("my_trips")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Sobre") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("about")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Menu Principal") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                if (locationState.currentTrip != null) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Map, contentDescription = "Roteiro") },
                            label = { Text("Roteiro") },
                            selected = false,
                            onClick = {
                                Toast.makeText(context, "Funcionalidade de Roteiro será implementada em outra tarefa.", Toast.LENGTH_LONG).show()
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Fotos") },
                            label = { Text("Fotos") },
                            selected = false,
                            onClick = {
                                val tripId = locationState.currentTrip?.id
                                if (tripId != null) {
                                    navController.navigate("trip_photos/$tripId")
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Olá, ${authViewModel.loggedInUser?.nome ?: "Visitante"}!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bem-vindo ao seu gerenciador de viagens pessoal.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (locationState.isLoading) {
                    CircularProgressIndicator()
                    Text("Buscando sua localização...", modifier = Modifier.padding(top = 8.dp))
                } else {
                    locationState.currentTrip?.let { trip ->
                        TripInfoCard(trip, locationState.city ?: "")
                        if (locationState.latitude != null && locationState.longitude != null) {
                            TripMap(latitude = locationState.latitude!!, longitude = locationState.longitude!!)
                        }
                    } ?: locationState.city?.let { city ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                "Você está em $city, mas não encontramos viagens para hoje neste local.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } ?: locationState.error?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                var showDebugInfo by remember { mutableStateOf(false) }
                
                TextButton(onClick = { showDebugInfo = !showDebugInfo }) {
                    Text(if (showDebugInfo) "Ocultar Debug" else "Mostrar Debug")
                }
                
                if (showDebugInfo) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("User ID: ${locationState.debugUserId}", style = MaterialTheme.typography.labelSmall)
                            Text("Lat: ${locationState.latitude}", style = MaterialTheme.typography.labelSmall)
                            Text("Lon: ${locationState.longitude}", style = MaterialTheme.typography.labelSmall)
                            Text("Cidade: ${locationState.city}", style = MaterialTheme.typography.labelSmall)
                            Text("Erro: ${locationState.error}", style = MaterialTheme.typography.labelSmall)
                            Text("Exceção: ${locationState.lastException}", style = MaterialTheme.typography.labelSmall)
                            Button(onClick = { authViewModel.loggedInUser?.id?.let { locationViewModel.fetchLocationAndTrip(it) } }) {
                                Text("Retry Location")
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { 
                        navController.navigate("login") {
                            popUpTo("menu") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair da conta")
                }
            }
        }
    }
}

@Composable
fun TripInfoCard(trip: Trip, city: String) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Viagem Atual detectada!", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            TripDetailRow("Destino", trip.destination)
            TripDetailRow("Data Início", dateFormat.format(Date(trip.startDate)))
            TripDetailRow("Data Final", dateFormat.format(Date(trip.endDate)))
            TripDetailRow("Tipo", trip.type)
            TripDetailRow("Orçamento", "R$ ${String.format(Locale.getDefault(), "%.2f", trip.budget)}")
            TripDetailRow("Total de Gastos", "R$ ${String.format(Locale.getDefault(), "%.2f", trip.totalExpenses)}")
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Localização atual: $city",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun TripDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TripMap(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background: #f0f0f0;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {zoomControl: false}).setView([$latitude, $longitude], 14);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OSM'
                }).addTo(map);
                L.marker([$latitude, $longitude]).addTo(map);
            </script>
        </body>
        </html>
    """.trimIndent()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
