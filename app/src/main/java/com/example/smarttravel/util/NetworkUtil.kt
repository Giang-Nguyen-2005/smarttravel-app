package com.example.smarttravel.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkQuality {
    NONE,        // Không có mạng
    POOR,        // Mạng yếu
    GOOD,        // Mạng tốt
    EXCELLENT    // Mạng rất tốt
}

@Singleton
class NetworkUtil @Inject constructor(
    private val context: Context
) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }
    
    fun getNetworkQuality(): NetworkQuality {
        if (!isNetworkAvailable()) {
            return NetworkQuality.NONE
        }
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkQuality.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.NONE
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    // Kiểm tra bandwidth
                    val downSpeed = capabilities.linkDownstreamBandwidthKbps
                    when {
                        downSpeed >= 10000 -> NetworkQuality.EXCELLENT
                        downSpeed >= 5000 -> NetworkQuality.GOOD
                        else -> NetworkQuality.POOR
                    }
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    val downSpeed = capabilities.linkDownstreamBandwidthKbps
                    when {
                        downSpeed >= 5000 -> NetworkQuality.GOOD
                        downSpeed >= 1000 -> NetworkQuality.POOR
                        else -> NetworkQuality.POOR
                    }
                }
                else -> NetworkQuality.GOOD
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkQuality.GOOD
                ConnectivityManager.TYPE_MOBILE -> NetworkQuality.POOR
                else -> NetworkQuality.POOR
            }
        }
    }
    
    fun shouldShowSkeleton(isLoading: Boolean, networkQuality: NetworkQuality): Boolean {
        return isLoading || networkQuality == NetworkQuality.NONE || networkQuality == NetworkQuality.POOR
    }
}



