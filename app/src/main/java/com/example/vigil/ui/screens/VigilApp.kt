package com.example.vigil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.ui.theme.VigilPrimary

/** Onboarding step order, then the tabbed main shell. */
private enum class Flow { Welcome, Overview, Facts, Privacy, Permissions, Main }

@Composable
fun VigilApp(modifier: Modifier = Modifier) {
    var step by remember { mutableStateOf(Flow.Welcome) }
    Box(modifier.fillMaxSize()) {
        when (step) {
            Flow.Welcome -> OnboardScaffold("Get Started", { step = Flow.Overview }) { WelcomeScreen() }
            Flow.Overview -> OnboardScaffold("Continue", { step = Flow.Facts }) { ProtectionOverviewScreen() }
            Flow.Facts -> OnboardScaffold("Continue", { step = Flow.Privacy }) { SafetyFactsScreen() }
            Flow.Privacy -> OnboardScaffold(
                "Next",
                { step = Flow.Permissions },
                secondary = {
                    Text(
                        "By tapping Next, you acknowledge our commitment to your digital sovereignty.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            ) { PrivacyCommitmentScreen() }
            Flow.Permissions -> OnboardScaffold(
                "Enable & Continue",
                { step = Flow.Main },
                secondary = {
                    TextButton(onClick = {}) {
                        Text("Learn how we handle data", color = VigilPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            ) { PermissionsScreen() }
            Flow.Main -> MainShell()
        }
    }
}

/**
 * Onboarding wrapper: content area plus a primary CTA anchored a fixed 24dp from the
 * bottom on every step, so the button lands at the same height across all screens.
 * An optional [secondary] element (caption or link) renders ABOVE the button so it
 * never shifts the button's vertical position.
 */
@Composable
private fun OnboardScaffold(
    buttonText: String,
    onNext: () -> Unit,
    secondary: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (secondary != null) {
                    secondary()
                    Spacer(Modifier.height(12.dp))
                }
                VigilPrimaryButton(text = buttonText, onClick = onNext)
            }
        }
    ) { pad -> Box(Modifier.padding(pad)) { content() } }
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
