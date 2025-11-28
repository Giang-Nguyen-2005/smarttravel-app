package com.example.smarttravel.di

import com.example.smarttravel.data.repository.AiService
import com.example.smarttravel.data.repository.AiServiceImpl
import com.example.smarttravel.data.repository.AuthRepository
import com.example.smarttravel.data.repository.AuthRepositoryImpl
import com.example.smarttravel.data.repository.DestinationRepository
import com.example.smarttravel.data.repository.DestinationRepositoryImpl
import com.example.smarttravel.data.repository.PlanRepository
import com.example.smarttravel.data.repository.PlanRepositoryImpl
import com.example.smarttravel.data.repository.CommentRepository
import com.example.smarttravel.data.repository.CommentRepositoryImpl
import android.content.Context
import com.example.smarttravel.util.NetworkUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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

    // Cung cấp NetworkUtil
    @Provides
    @Singleton
    fun provideNetworkUtil(@ApplicationContext context: Context): NetworkUtil {
        return NetworkUtil(context)
    }
    
    // Cung cấp DestinationRepository
    @Provides
    @Singleton
    fun provideDestinationRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        networkUtil: NetworkUtil
    ): DestinationRepository {
        return DestinationRepositoryImpl(firestore, firebaseAuth, networkUtil)
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

    // Cung cấp PlanRepository
    @Provides
    @Singleton
    fun providePlanRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): PlanRepository {
        return PlanRepositoryImpl(firestore, firebaseAuth)
    }
    
    // Cung cấp AiService
    @Provides
    @Singleton
    fun provideAiService(): AiService {
        return AiServiceImpl()
    }

    // Cung cấp CommentRepository
    @Provides
    @Singleton
    fun provideCommentRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): CommentRepository {
        return CommentRepositoryImpl(firebaseAuth, firestore)
    }

}