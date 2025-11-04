package com.example.smarttravel.di

import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Các dependencies trong module này sẽ sống chung với Application
object AppModule {

    // Hướng dẫn Hilt cách cung cấp một instance của FirebaseAuth
    @Provides
    @Singleton // Chỉ tạo một instance duy nhất trong suốt vòng đời ứng dụng
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Hướng dẫn Hilt cách cung cấp một instance của AuthRepository
    // Hilt sẽ tự động lấy FirebaseAuth đã được cung cấp ở trên và truyền vào AuthRepositoryImpl
    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }
}