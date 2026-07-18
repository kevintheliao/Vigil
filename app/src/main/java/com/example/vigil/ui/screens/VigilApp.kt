package com.example.vigil.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.vigil.detection.DetectionLogEntry
import com.example.vigil.detection.DetectionOverlayService
import com.example.vigil.ui.theme.VigilPrimary

// Onboarding step order 
private enum class Flow { Welcome, Facts, Overview, Privacy, Permissions, OverlayPermission, UsageAccess, Main }

// Set to true once onboarding testing is done: returning users then skip straight
private const val ONBOARDING_PERSISTENCE_ENABLED = false

@Composable
fun VigilApp(
    modifier: Modifier = Modifier,
    analysisArgs: AnalysisArgs? = null,
    onAnalysisDismissed: () -> Unit = {},
) {
    val context = LocalContext.current
    var step by remember {
        mutableStateOf(
            if (analysisArgs != null || (ONBOARDING_PERSISTENCE_ENABLED && OnboardingPrefs.isCompleted(context))) Flow.Main
            else Flow.Welcome
        )
    }
    //chip tap while onboarding is on screen: jump to Main so the analysis can show
    LaunchedEffect(analysisArgs) { if (analysisArgs != null) step = Flow.Main }
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
    // Overlay permission is a Settings toggle, not a dialog: launch Settings, continue on return.
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        step = Flow.UsageAccess
    }
    // Same pattern for "Usage access".
    val usageAccessLauncher = rememberLauncherForActivityResult(
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
                            step = Flow.UsageAccess
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
                        TextButton(onClick = { step = Flow.UsageAccess }) {
                            Text("Maybe later", color = VigilPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) { OverlayPermissionScreen() }
                Flow.UsageAccess -> OnboardScaffold(
                    "Allow & Continue",
                    {
                        if (DetectionOverlayService.hasUsageAccess(context)) {
                            completeOnboarding()
                        } else {
                            usageAccessLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    },
                    secondary = {
                        TextButton(onClick = completeOnboarding) {
                            Text("Maybe later", color = VigilPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                ) { UsageAccessScreen() }
                Flow.Main -> MainShell(
                    permissionGranted = smsPermissionGranted,
                    onRequestPermission = { smsPermissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) },
                    analysisArgs = analysisArgs,
                    onAnalysisDismissed = onAnalysisDismissed,
                )
            }
        }
    }
}

/** Onboarding wrapper: pins the CTA at a fixed height on every step, with an optional [secondary] slot above it. */
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

private enum class Tab { Home, Logs, Education }

@Composable
private fun MainShell(
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    analysisArgs: AnalysisArgs? = null,
    onAnalysisDismissed: () -> Unit = {},
) {
    var tab by remember { mutableStateOf(Tab.Home) }
    //log-row taps open the same screen; chip-tap args (activity intent) take precedence
    var logAnalysis by remember { mutableStateOf<AnalysisArgs?>(null) }
    val shownAnalysis = analysisArgs ?: logAnalysis
    val dismiss = {
        logAnalysis = null
        onAnalysisDismissed()
    }
    //hold the last args so the screen still has content during the exit slide
    var lastAnalysis by remember { mutableStateOf<AnalysisArgs?>(null) }
    if (shownAnalysis != null) {
        lastAnalysis = shownAnalysis
        BackHandler(onBack = dismiss)
    }

    AnimatedContent(
        targetState = shownAnalysis != null,
        transitionSpec = {
            if (targetState) {
                //analysis slides up over the shell
                slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it / 4 } +
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            } else {
                //back: analysis slides down and fades, shell fades in underneath
                fadeIn(tween(200)) togetherWith
                    slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it / 4 } +
                    fadeOut(tween(200))
            }
        },
        label = "analysis-transition",
    ) { showAnalysis ->
        if (showAnalysis) {
            lastAnalysis?.let { AnalysisScreen(args = it, onBack = dismiss) }
        } else {
            MainTabs(tab, { tab = it }, permissionGranted, onRequestPermission, onEntryClick = { logAnalysis = it.toAnalysisArgs() })
        }
    }
}

@Composable
private fun MainTabs(
    tab: Tab,
    onTabChange: (Tab) -> Unit,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onEntryClick: (DetectionLogEntry) -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Home,
                    onClick = { onTabChange(Tab.Home) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Logs,
                    onClick = { onTabChange(Tab.Logs) },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Logs") },
                    label = { Text("Logs") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Education,
                    onClick = { onTabChange(Tab.Education) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Education") },
                    label = { Text("Education") }
                )
            }
        }
    ) { pad ->
        val inner = Modifier.fillMaxSize().padding(pad)
        when (tab) {
            Tab.Home -> HomeScreen(
                inner, permissionGranted, onRequestPermission,
                onViewAll = { onTabChange(Tab.Logs) },
                onEntryClick = onEntryClick,
            )
            Tab.Logs -> AllLogsScreen(inner, onEntryClick = onEntryClick)
            Tab.Education -> EducationScreen(inner)
        }
    }
}
