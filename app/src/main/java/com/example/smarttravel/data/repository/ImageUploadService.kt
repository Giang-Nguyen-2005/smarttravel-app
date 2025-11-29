package com.example.smarttravel.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadService @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    suspend fun uploadImage(uri: Uri, folder: String = "destinations"): Result<String> {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef: StorageReference = storage.reference.child("$folder/$fileName")
            
            android.util.Log.d("ImageUploadService", "Uploading image to: $folder/$fileName")
            android.util.Log.d("ImageUploadService", "Uri: $uri")
            
            // Kiểm tra xem Storage bucket có tồn tại không
            try {
                // Upload file
                val uploadTask = storageRef.putFile(uri).await()
                android.util.Log.d("ImageUploadService", "Upload completed, getting download URL...")
                
                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await()
                
                android.util.Log.d("ImageUploadService", "Download URL: $downloadUrl")
                Result.success(downloadUrl.toString())
            } catch (storageException: com.google.firebase.storage.StorageException) {
                val errorCode = storageException.errorCode
                val errorMessage = when (errorCode) {
                    -13010 -> { // ERROR_OBJECT_NOT_FOUND
                        "Firebase Storage chưa được bật. Vui lòng vào Firebase Console > Storage > Get started để tạo bucket."
                    }
                    -13020 -> { // ERROR_UNAUTHORIZED
                        "Không có quyền upload. Vui lòng kiểm tra Security Rules."
                    }
                    else -> {
                        "Lỗi upload: ${storageException.message}"
                    }
                }
                android.util.Log.e("ImageUploadService", "Storage error: $errorMessage", storageException)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("404") == true || e.message?.contains("Not Found") == true -> {
                    "Firebase Storage chưa được bật. Vui lòng:\n1. Vào Firebase Console\n2. Chọn Storage\n3. Nhấn 'Get started' để tạo bucket"
                }
                e.message?.contains("permission") == true || e.message?.contains("unauthorized") == true -> {
                    "Không có quyền upload. Vui lòng kiểm tra Security Rules trong Firebase Console."
                }
                else -> {
                    "Lỗi khi upload ảnh: ${e.message ?: "Lỗi không xác định"}"
                }
            }
            android.util.Log.e("ImageUploadService", "Error uploading image: $errorMsg", e)
            Result.failure(Exception(errorMsg))
        }
    }
    
    suspend fun uploadMultipleImages(uris: List<Uri>, folder: String = "destinations"): Result<List<String>> {
        return try {
            val urls = mutableListOf<String>()
            for (uri in uris) {
                val result = uploadImage(uri, folder)
                if (result.isSuccess) {
                    urls.add(result.getOrNull()!!)
                } else {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Upload failed"))
                }
            }
            Result.success(urls)
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadService", "Error uploading multiple images: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadService", "Error deleting image: ${e.message}", e)
            Result.failure(e)
        }
    }
}

