package com.example.smarttravel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.smarttravel.navigation.AppNavigation // Import AppNavigation
import com.example.smarttravel.ui.theme.SmarttravelTheme
import dagger.hilt.android.AndroidEntryPoint // <-- THÊM DÒNG NÀY

@AndroidEntryPoint // <-- Đánh dấu Activity này là entry point cho Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Dùng cho full screen content
        setContent {
            SmarttravelTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController) // Sử dụng AppNavigation của bạn
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SmarttravelTheme {
        val navController = rememberNavController()
        AppNavigation(navController = navController)
    }
}