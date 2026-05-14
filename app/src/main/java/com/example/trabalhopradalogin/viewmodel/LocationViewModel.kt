package com.example.trabalhopradalogin.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhopradalogin.data.AppDatabase
import com.example.trabalhopradalogin.data.Trip
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.util.Locale

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val currentTrip: Trip? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastException: String? = null,
    val debugUserId: Int? = null
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = mutableStateOf(LocationState())
    val state: State<LocationState> = _state

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val tripDao = AppDatabase.getDatabase(application).tripDao()

    @SuppressLint("MissingPermission")
    fun fetchLocationAndTrip(userId: Int) {
        _state.value = _state.value.copy(isLoading = true, error = null, lastException = null, debugUserId = userId)
        
        viewModelScope.launch {
            try {
                // Tenta primeiro a última localização conhecida (mais rápido)
                var location = fusedLocationClient.lastLocation.await()
                
                // Se não tiver, ou se quiser forçar uma nova, usa getCurrentLocation
                if (location == null) {
                    location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).await()
                }

                if (location != null) {
                    _state.value = _state.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    
                    val city = getCityName(location.latitude, location.longitude)
                    if (city != null) {
                        val currentTime = System.currentTimeMillis()
                        // Busca com a cidade limpa (sem espaços) e a query melhorada no DAO
                        val trip = tripDao.findCurrentTripByCity(userId, city.trim(), currentTime)
                        _state.value = _state.value.copy(
                            city = city,
                            currentTrip = trip,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(
                            error = "Falha ao determinar cidade (Geocoder e Web falharam).",
                            isLoading = false
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        error = "Não foi possível obter nenhuma localização (GPS pode estar desligado).",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Exceção ao obter localização.",
                    lastException = e.message ?: e.toString(),
                    isLoading = false
                )
            }
        }
    }

    private suspend fun getCityName(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        // 1. Tenta o Geocoder do Android primeiro
        val geocoderCity = try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()
                address?.locality ?: address?.subAdminArea ?: address?.adminArea
            } else null
        } catch (e: Exception) {
            null
        }

        if (geocoderCity != null) return@withContext geocoderCity

        // 2. Se o Geocoder falhar (comum em emuladores), tenta o Fallback via Web Service (Nominatim)
        return@withContext getCityNameWebFallback(lat, lon)
    }

    private suspend fun getCityNameWebFallback(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=10")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "TrabalhoPradaApp")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val address = json.optJSONObject("address")
            
            address?.optString("city") 
                ?: address?.optString("town") 
                ?: address?.optString("village")
                ?: address?.optString("municipality")
                ?: address?.optString("suburb")
        } catch (e: Exception) {
            null
        }
    }
}
