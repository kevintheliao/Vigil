package com.example.vigil.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Onboarding step order, then the tabbed main shell. */
private enum class Flow { Welcome, Overview, Facts, Privacy, Permissions, Main }

@Composable
fun VigilApp(modifier: Modifier = Modifier) {
    var step by remember { mutableStateOf(Flow.Welcome) }
    androidx.compose.foundation.layout.Box(modifier.fillMaxSize()) {
        when (step) {
            Flow.Welcome -> WelcomeScreen(onGetStarted = { step = Flow.Overview })
            Flow.Overview -> OnboardStep({ ProtectionOverviewScreen() }, { step = Flow.Facts })
            Flow.Facts -> OnboardStep({ SafetyFactsScreen() }, { step = Flow.Privacy })
            Flow.Privacy -> PrivacyCommitmentScreen(onNext = { step = Flow.Permissions })
            Flow.Permissions -> PermissionsScreen(onEnable = { step = Flow.Main })
            Flow.Main -> MainShell()
        }
    }
}

/** Wraps a content-only onboarding slide with a Continue button. */
@Composable
private fun OnboardStep(content: @Composable () -> Unit, onNext: () -> Unit) {
    Scaffold(
        bottomBar = {
            VigilPrimaryButton(
                text = "Continue",
                onClick = onNext,
                modifier = Modifier.padding(24.dp)
            )
        }
    ) { pad -> androidx.compose.foundation.layout.Box(Modifier.padding(pad)) { content() } }
}

private enum class Tab { Home, Education }

@Composable
private fun MainShell() {
    var tab by remember { mutableStateOf(Tab.Home) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Home,
                    onClick = { tab = Tab.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Education,
                    onClick = { tab = Tab.Education },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Education") },
                    label = { Text("Education") }
                )
            }
        }
    ) { pad ->
        val inner = Modifier.fillMaxSize().padding(pad)
        when (tab) {
            Tab.Home -> HomeScreen(inner)
            Tab.Education -> EducationScreen(inner)
        }
    }
}
