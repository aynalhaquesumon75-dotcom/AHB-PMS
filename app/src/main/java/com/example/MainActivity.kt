package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.i18n.AppLanguage
import com.example.i18n.AppStringsBn
import com.example.i18n.AppStringsEn
import com.example.i18n.LocalStrings
import com.example.ui.AhbPmsApp
import com.example.ui.PmsViewModel
import com.example.ui.theme.AhbPmsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val viewModel: PmsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val strings = if (uiState.language == AppLanguage.BN) AppStringsBn else AppStringsEn

            AhbPmsTheme(language = uiState.language) {
                CompositionLocalProvider(LocalStrings provides strings) {
                    AhbPmsApp(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}
