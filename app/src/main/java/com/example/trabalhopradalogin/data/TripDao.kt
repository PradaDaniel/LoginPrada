package com.example.trabalhopradalogin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE userId = :userId")
    fun getTripsByUserId(userId: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE userId = :userId AND destination LIKE '%' || :city || '%' COLLATE NOCASE AND :currentDate >= startDate AND :currentDate <= (endDate + 86400000) LIMIT 1")
    suspend fun findCurrentTripByCity(userId: Int, city: String, currentDate: Long): Trip?
}
