package com.example.trabalhopradalogin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripPhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: TripPhoto)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY id DESC")
    fun getPhotosForTrip(tripId: Int): Flow<List<TripPhoto>>
}
