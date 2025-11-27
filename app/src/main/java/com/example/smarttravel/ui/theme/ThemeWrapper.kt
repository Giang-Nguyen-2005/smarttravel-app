package com.example.smarttravel.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttravel.ui.viewmodel.SettingsViewModel

@Composable
fun ThemeWrapper(
    content: @Composable () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeMode by settingsViewModel.themeMode.collectAsState()
    
    SmarttravelTheme(themeMode = themeMode) {
        content()
    }
}

