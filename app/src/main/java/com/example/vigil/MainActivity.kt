package com.example.vigil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vigil.ui.screens.VigilApp
import com.example.vigil.ui.theme.VigilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VigilTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VigilApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
