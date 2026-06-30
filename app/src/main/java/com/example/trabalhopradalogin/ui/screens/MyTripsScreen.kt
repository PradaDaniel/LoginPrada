package com.example.trabalhopradalogin.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trabalhopradalogin.data.Trip
import com.example.trabalhopradalogin.viewmodel.AuthViewModel
import com.example.trabalhopradalogin.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyTripsScreen(
    navController: NavController,
    tripViewModel: TripViewModel,
    authViewModel: AuthViewModel
) {
    val userId = authViewModel.loggedInUser?.id ?: 0
    val trips by tripViewModel.getTrips(userId).collectAsState(initial = emptyList())
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }
    var tripToEdit by remember { mutableStateOf<Trip?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Viagens") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (trips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Nenhuma viagem cadastrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trips) { trip ->
                    TripCard(
                        trip = trip,
                        dateFormatter = dateFormatter,
                        onDelete = { tripToDelete = trip },
                        onEdit = { tripToEdit = trip }
                    )
                }
            }
        }
        
        // Diálogo de Exclusão
        if (tripToDelete != null) {
            AlertDialog(
                onDismissRequest = { tripToDelete = null },
                title = { Text("Excluir Viagem") },
                text = { Text("Tem certeza que deseja excluir a viagem para ${tripToDelete?.destination}?") },
                confirmButton = {
                    TextButton(onClick = {
                        tripToDelete?.let { tripViewModel.deleteTrip(it) }
                        tripToDelete = null
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { tripToDelete = null }) { Text("Cancelar") }
                }
            )
        }
        
        // Diálogo de Edição (Simplificado para o exercício)
        if (tripToEdit != null) {
            EditTripDialog(
                trip = tripToEdit!!,
                onDismiss = { tripToEdit = null },
                onSave = { updatedTrip ->
                    tripViewModel.updateTrip(updatedTrip) { success ->
                        if (success) tripToEdit = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripCard(
    trip: Trip,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val icon: ImageVector = if (trip.type == "Lazer") Icons.Default.BeachAccess else Icons.Default.BusinessCenter
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Poderia abrir detalhes */ },
                onLongClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = trip.type,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.destination, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${dateFormatter.format(Date(trip.startDate))} - ${dateFormatter.format(Date(trip.endDate))}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Orçamento: R$ ${"%.2f".format(trip.budget)}", style = MaterialTheme.typography.bodySmall)
            }
            
            // "tap on the left or right side" - aqui no lado direito
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit
) {
    var destination by remember { mutableStateOf(trip.destination) }
    var type by remember { mutableStateOf(trip.type) }
    var budget by remember { mutableStateOf(trip.budget.toString()) }
    
    var startDate by remember { mutableLongStateOf(trip.startDate) }
    var endDate by remember { mutableLongStateOf(trip.endDate) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Viagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destino") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Tipo de Viagem", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "Lazer", onClick = { type = "Lazer" })
                        Text("Lazer")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "Negócios", onClick = { type = "Negócios" })
                        Text("Negócios")
                    }
                }
                
                Box(modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true }) {
                    OutlinedTextField(
                        value = dateFormatter.format(Date(startDate)),
                        onValueChange = {},
                        label = { Text("Data Início") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    )
                }
                
                Box(modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true }) {
                    OutlinedTextField(
                        value = dateFormatter.format(Date(endDate)),
                        onValueChange = {},
                        label = { Text("Data Fim") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    )
                }
                
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Orçamento") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    trip.copy(
                        destination = destination,
                        type = type,
                        startDate = startDate,
                        endDate = endDate,
                        budget = budget.toDoubleOrNull() ?: trip.budget
                    )
                )
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis ?: startDate
                    showStartDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = datePickerState.selectedDateMillis ?: endDate
                    showEndDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
