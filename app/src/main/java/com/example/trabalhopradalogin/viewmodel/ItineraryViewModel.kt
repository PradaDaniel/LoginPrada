package com.example.trabalhopradalogin.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trabalhopradalogin.data.Trip
import com.example.trabalhopradalogin.data.TripDao
import com.example.trabalhopradalogin.data.repository.ItineraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ItineraryUiState {
    object Idle : ItineraryUiState
    object Loading : ItineraryUiState
    data class Success(val itinerary: String) : ItineraryUiState
    data class Error(val message: String) : ItineraryUiState
}

class ItineraryViewModel(
    private val repository: ItineraryRepository,
    private val tripDao: TripDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItineraryUiState>(ItineraryUiState.Idle)
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    // Form inputs
    val destination = mutableStateOf("")
    val duration = mutableStateOf("")
    val interests = mutableStateOf("")
    val travelStyle = mutableStateOf("Lazer") // Default
    val budgetLevel = mutableStateOf("Moderado") // Default

    // Associated trip
    private val _associatedTrip = MutableStateFlow<Trip?>(null)
    val associatedTrip: StateFlow<Trip?> = _associatedTrip.asStateFlow()

    fun loadTripDetails(tripId: Int) {
        viewModelScope.launch {
            val trip = tripDao.getTripById(tripId)
            if (trip != null) {
                _associatedTrip.value = trip
                destination.value = trip.destination
                
                // Formating duration
                val diffTime = trip.endDate - trip.startDate
                val diffDays = (diffTime / (1000 * 60 * 60 * 24)).toInt() + 1
                duration.value = if (diffDays > 0) "$diffDays dia(s)" else ""
                
                travelStyle.value = trip.type

                // If trip already has a saved itinerary, display it
                if (!trip.itinerary.isNullOrBlank()) {
                    _uiState.value = ItineraryUiState.Success(trip.itinerary)
                }
            }
        }
    }

    fun generateItinerary() {
        if (destination.value.isBlank()) {
            _uiState.value = ItineraryUiState.Error("O destino é obrigatório.")
            return
        }

        _uiState.value = ItineraryUiState.Loading

        viewModelScope.launch {
            val result = repository.generateItinerary(
                destination = destination.value,
                duration = duration.value,
                interests = interests.value,
                style = travelStyle.value,
                budget = budgetLevel.value
            )

            result.fold(
                onSuccess = { itinerary ->
                    _uiState.value = ItineraryUiState.Success(itinerary)
                },
                onFailure = { error ->
                    _uiState.value = ItineraryUiState.Error(
                        error.localizedMessage ?: "Erro desconhecido ao gerar o roteiro."
                    )
                }
            )
        }
    }

    fun saveItineraryToTrip(onComplete: (Boolean) -> Unit) {
        val trip = _associatedTrip.value
        val state = _uiState.value
        if (trip != null && state is ItineraryUiState.Success) {
            viewModelScope.launch {
                try {
                    val updatedTrip = trip.copy(itinerary = state.itinerary)
                    tripDao.updateTrip(updatedTrip)
                    _associatedTrip.value = updatedTrip
                    onComplete(true)
                } catch (e: Exception) {
                    onComplete(false)
                }
            }
        } else {
            onComplete(false)
        }
    }

    fun resetState() {
        _uiState.value = ItineraryUiState.Idle
    }
}

class ItineraryViewModelFactory(
    private val repository: ItineraryRepository,
    private val tripDao: TripDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItineraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItineraryViewModel(repository, tripDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
