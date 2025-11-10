package com.example.smarttravel.di

import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.AuthRepositoryImpl
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.DestinationRepositoryImpl
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.data.repository.PlanRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule { // <-- Dòng 19

    // Cung cấp FirebaseAuth
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Cung cấp FirebaseFirestore
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // Cung cấp DestinationRepository
    @Provides
    @Singleton
    fun provideDestinationRepository(firestore: FirebaseFirestore): DestinationRepository {
        return DestinationRepositoryImpl(firestore)
    }

    // Cung cấp AuthRepository
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    // Cung cấp PlanRepository (mới)
    @Provides
    @Singleton
    fun providePlanRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): PlanRepository {
        return PlanRepositoryImpl(firestore, firebaseAuth) // Sửa lỗi logic: cần firestore và firebaseAuth
    }

}