package com.example.trabalhopradalogin.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhopradalogin.data.AppDatabase
import com.example.trabalhopradalogin.data.TripPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class TripPhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val tripPhotoDao = AppDatabase.getDatabase(application).tripPhotoDao()

    fun getPhotosForTrip(tripId: Int): Flow<List<TripPhoto>> {
        return tripPhotoDao.getPhotosForTrip(tripId)
    }

    fun addPhotoToTrip(tripId: Int, uri: Uri, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val contentResolver = context.contentResolver
                
                val photoFolder = File(context.filesDir, "trip_photos")
                if (!photoFolder.exists()) {
                    photoFolder.mkdirs()
                }
                
                val fileName = "photo_${tripId}_${UUID.randomUUID()}.jpg"
                val file = File(photoFolder, fileName)
                
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                val savedUriString = Uri.fromFile(file).toString()
                val tripPhoto = TripPhoto(tripId = tripId, photoUri = savedUriString)
                tripPhotoDao.insertPhoto(tripPhoto)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCapturedPhotoToTrip(tripId: Int, file: File, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val photoFolder = File(context.filesDir, "trip_photos")
                if (!photoFolder.exists()) {
                    photoFolder.mkdirs()
                }
                
                val finalFile = File(photoFolder, "photo_${tripId}_${UUID.randomUUID()}.jpg")
                file.renameTo(finalFile)
                
                val savedUriString = Uri.fromFile(finalFile).toString()
                val tripPhoto = TripPhoto(tripId = tripId, photoUri = savedUriString)
                tripPhotoDao.insertPhoto(tripPhoto)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
