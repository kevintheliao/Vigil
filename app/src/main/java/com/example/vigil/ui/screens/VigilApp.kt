package com.example.vigil.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.vigil.OnboardingPrefs
import com.example.vigil.ui.theme.VigilPrimary

// Onboarding step order 
private enum class Flow { Welcome, Facts, Overview, Privacy, Permissions, OverlayPermission, Main }

// Set to true once onboarding testing is done: returning users then skip straight
private const val ONBOARDING_PERSISTENCE_ENABLED = false

@Composable
fun VigilApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var step by remember {
        mutableStateOf(
            if (ONBOARDING_PERSISTENCE_ENABLED && OnboardingPrefs.isCompleted(context)) Flow.Main
            else Flow.Welcome
        )
    }
    var smsPermissionGranted by remember { mutableStateOf(true) }
    val completeOnboarding = {
        OnboardingPrefs.setCompleted(context)
        step = Flow.Main
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        smsPermissionGranted = results.values.all { it }
        step = Flow.OverlayPermission
    }
    // "Display over other apps" has no runtime dialog — it's a Settings toggle,
    // so we launch the Settings page and move on when the user comes back.
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        completeOnboarding()
    }
    Box(modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState.ordinal >= initialState.ordinal
                val slideDir = if (forward) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
                slideIntoContainer(slideDir, animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                    fadeIn(animationSpec = tween(250, delayMillis = 90)) togetherWith
                    slideOutOfContainer(slideDir, animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                    fadeOut(animationSpec = tween(90))
            },
            label = "onboarding-transition"
        ) { animatedStep ->
            when (animatedStep) {
                Flow.Welcome -> OnboardScaffold("Get Started", { step = Flow.Facts }) { WelcomeScreen() }
                Flow.Facts -> OnboardScaffold(
                    "Continue",
                    { step = Flow.Overview },
                    secondary = { SafetyFactsSources() }
                ) { SafetyFactsScreen() }
                Flow.Overview -> OnboardScaffold("Continue", { step = Flow.Privacy }) { ProtectionOverviewScreen() }
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
                    { smsPermissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) },
                    secondary = {
                        TextButton(onClick = {}) {
                            Text("Learn how we handle data", color = VigilPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) { PermissionsScreen() }
                Flow.OverlayPermission -> OnboardScaffold(
                    "Allow & Continue",
                    {
                        if (Settings.canDrawOverlays(context)) {
                            completeOnboarding()
                        } else {
                            overlayPermissionLauncher.launch(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri()
                                )
                            )
                        }
                    },
                    secondary = {
                        TextButton(onClick = completeOnboarding) {
                            Text("Maybe later", color = VigilPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) { OverlayPermissionScreen() }
                Flow.Main -> MainShell(
                    permissionGranted = smsPermissionGranted,
                    onRequestPermission = { smsPermissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) }
                )
            }
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
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 40.dp),
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
private fun MainShell(permissionGranted: Boolean, onRequestPermission: () -> Unit) {
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
            Tab.Home -> HomeScreen(inner, permissionGranted, onRequestPermission)
            Tab.Education -> EducationScreen(inner)
        }
    }
}
