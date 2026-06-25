package com.example.trabalhopradalogin.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trabalhopradalogin.viewmodel.ItineraryUiState
import com.example.trabalhopradalogin.viewmodel.ItineraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    navController: NavController,
    viewModel: ItineraryViewModel,
    tripId: Int? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val associatedTrip by viewModel.associatedTrip.collectAsState()

    var destination by viewModel.destination
    var duration by viewModel.duration
    var interests by viewModel.interests
    var travelStyle by viewModel.travelStyle
    var budgetLevel by viewModel.budgetLevel

    LaunchedEffect(tripId) {
        if (tripId != null && tripId != 0) {
            viewModel.loadTripDetails(tripId)
        } else {
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roteiro de Viagem IA") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ItineraryUiState.Idle -> {
                    ItineraryForm(
                        destination = destination,
                        onDestinationChange = { destination = it },
                        duration = duration,
                        onDurationChange = { duration = it },
                        interests = interests,
                        onInterestsChange = { interests = it },
                        travelStyle = travelStyle,
                        onTravelStyleChange = { travelStyle = it },
                        budgetLevel = budgetLevel,
                        onBudgetLevelChange = { budgetLevel = it },
                        onGenerateClick = { viewModel.generateItinerary() }
                    )
                }
                is ItineraryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "O Gemini está criando seu roteiro personalizado...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Buscando os melhores pontos turísticos, restaurantes e atividades locais.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
                is ItineraryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (associatedTrip != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Text(
                                    text = "Roteiro associado à viagem para ${associatedTrip?.destination}",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = state.itinerary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Roteiro de Viagem", state.itinerary)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Roteiro copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar")
                            }

                            if (associatedTrip != null) {
                                Button(
                                    onClick = {
                                        viewModel.saveItineraryToTrip { success ->
                                            if (success) {
                                                Toast.makeText(context, "Roteiro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Erro ao salvar roteiro.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Salvar")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ajustar dados / Gerar Novo")
                        }
                    }
                }
                is ItineraryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Ops! Ocorreu um erro",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Tentar Novamente")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryForm(
    destination: String,
    onDestinationChange: (String) -> Unit,
    duration: String,
    onDurationChange: (String) -> Unit,
    interests: String,
    onInterestsChange: (String) -> Unit,
    travelStyle: String,
    onTravelStyleChange: (String) -> Unit,
    budgetLevel: String,
    onBudgetLevelChange: (String) -> Unit,
    onGenerateClick: () -> Unit
) {
    val styles = listOf("Lazer", "Negócios", "Cultura", "Aventura", "Família")
    val budgets = listOf("Econômico", "Moderado", "Luxo")
    
    var styleExpanded by remember { mutableStateOf(false) }
    var budgetExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Informe os detalhes abaixo para que nossa inteligência artificial crie um roteiro dia a dia perfeito para você.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            label = { Text("Destino (Cidade, País)") },
            placeholder = { Text("Ex: Paris, França") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = duration,
            onValueChange = onDurationChange,
            label = { Text("Período / Duração") },
            placeholder = { Text("Ex: 5 dias, Fim de semana") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        ExposedDropdownMenuBox(
            expanded = styleExpanded,
            onExpandedChange = { styleExpanded = it }
        ) {
            OutlinedTextField(
                value = travelStyle,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estilo da Viagem") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = styleExpanded,
                onDismissRequest = { styleExpanded = false }
            ) {
                styles.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onTravelStyleChange(selectionOption)
                            styleExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = budgetExpanded,
            onExpandedChange = { budgetExpanded = it }
        ) {
            OutlinedTextField(
                value = budgetLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Orçamento Estimado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = budgetExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = budgetExpanded,
                onDismissRequest = { budgetExpanded = false }
            ) {
                budgets.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onBudgetLevelChange(selectionOption)
                            budgetExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = interests,
            onValueChange = onInterestsChange,
            label = { Text("Seus interesses (Atividades, Gostos)") },
            placeholder = { Text("Ex: Museus de arte, culinária local, parques ecológicos, compras") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gerar Roteiro com Gemini AI", fontWeight = FontWeight.Bold)
        }
    }
}
