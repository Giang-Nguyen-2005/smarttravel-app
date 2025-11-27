package com.example.smarttravel.data.repository

import com.example.smarttravel.data.model.UserRating
import com.example.smarttravel.model.Destination
import com.example.smarttravel.model.Category
import kotlinx.coroutines.flow.Flow

interface DestinationRepository {
    fun getCategories(): Flow<Result<List<Category>>>
    fun getDestinations(): Flow<Result<List<Destination>>>

    fun getDestinationById(id: String): Flow<Result<Destination>>

    fun searchDestinations(query: String): Flow<Result<List<Destination>>>
    
    fun getDestinationsByIds(ids: List<String>): Flow<Result<List<Destination>>>
    
    // Rating methods
    suspend fun saveUserRating(destinationId: String, userId: String, rating: Double): Result<Unit>
    suspend fun getUserRating(destinationId: String, userId: String): Result<UserRating?>
    fun getAverageRating(destinationId: String): Flow<Result<Double>>
    suspend fun updateDestinationRating(destinationId: String): Result<Unit>
}