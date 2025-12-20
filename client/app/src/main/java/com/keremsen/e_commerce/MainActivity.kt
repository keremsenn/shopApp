package com.keremsen.e_commerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
// Kütüphaneyi import et
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.keremsen.e_commerce.navigation.NavGraph
import com.keremsen.e_commerce.ui.theme.E_commerceTheme
import com.keremsen.e_commerce.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            viewModel.startDestination.value == null
        }

        enableEdgeToEdge()
        setContent {
            E_commerceTheme {
                val startDestination by viewModel.startDestination.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (startDestination != null) {
                        NavGraph(startDestination = startDestination!!)
                    }
                }
            }
        }
    }
}