package com.example.smarttravel.di

import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.AuthRepositoryImpl
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.DestinationRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideDestinationRepository(firestore: FirebaseFirestore): DestinationRepository {
        return DestinationRepositoryImpl(firestore)
    }

    // Thêm tham số firestore vào đây để Hilt biết cách tạo AuthRepositoryImpl mới
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }
}