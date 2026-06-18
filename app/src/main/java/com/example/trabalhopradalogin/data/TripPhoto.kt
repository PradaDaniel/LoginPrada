package com.example.trabalhopradalogin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_photos")
data class TripPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val photoUri: String
)
