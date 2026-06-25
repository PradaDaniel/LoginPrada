package com.example.trabalhopradalogin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination: String,
    val type: String, // "Lazer" or "Negócios"
    val startDate: Long,
    val endDate: Long,
    val budget: Double,
    val totalExpenses: Double = 0.0,
    val userId: Int,
    val itinerary: String? = null
)
