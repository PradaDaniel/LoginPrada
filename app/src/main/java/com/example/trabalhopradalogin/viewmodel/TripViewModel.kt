package com.example.trabalhopradalogin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhopradalogin.data.Trip
import com.example.trabalhopradalogin.data.TripDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TripViewModel(private val tripDao: TripDao) : ViewModel() {

    fun getTrips(userId: Int): Flow<List<Trip>> {
        return tripDao.getTripsByUserId(userId)
    }

    fun addTrip(trip: Trip, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                tripDao.insertTrip(trip)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateTrip(trip: Trip, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                tripDao.updateTrip(trip)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.deleteTrip(trip)
        }
    }
}
