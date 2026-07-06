package com.example.vigil.detection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val EXIT_ANIMATION_MILLIS = 250
const val DEFAULT_AUTO_DISMISS_MILLIS = 4_000L

/** Resolved colors for one severity level in the current theme. */
private data class SeverityColors(
    val accent: Color,
    val iconBackground: Color,
)

private fun severityColors(severity: Severity, darkTheme: Boolean): SeverityColors = when (severity) {
    Severity.SAFE -> if (darkTheme) {
        SeverityColors(accent = Color(0xFF6DD58C), iconBackground = Color(0xFF1B3B25))
    } else {
        SeverityColors(accent = Color(0xFF1B873B), iconBackground = Color(0xFFDCF2E3))
    }

    Severity.MEDIUM -> if (darkTheme) {
        SeverityColors(accent = Color(0xFFFFB74D), iconBackground = Color(0xFF3E2E12))
    } else {
        SeverityColors(accent = Color(0xFFB26A00), iconBackground = Color(0xFFFFEBCC))
    }

    Severity.HIGH -> if (darkTheme) {
        SeverityColors(accent = Color(0xFFFF6D66), iconBackground = Color(0xFF44201E))
    } else {
        SeverityColors(accent = Color(0xFFC62828), iconBackground = Color(0xFFFFDDDA))
    }

    Severity.UNKNOWN -> if (darkTheme) {
        SeverityColors(accent = Color(0xFFB0B4BA), iconBackground = Color(0xFF2E3236))
    } else {
        SeverityColors(accent = Color(0xFF5F6672), iconBackground = Color(0xFFE7E9EC))
    }
}

private fun severityIcon(severity: Severity): ImageVector = when (severity) {
    Severity.SAFE -> Icons.Rounded.VerifiedUser
    Severity.MEDIUM -> Icons.Rounded.Report
    Severity.HIGH -> Icons.Rounded.GppMaybe
    Severity.UNKNOWN -> Icons.Rounded.HelpOutline
}

/**
 * Minimal detection indicator overlay: fades in + slides up from the bottom,
 * auto-dismisses after [autoDismissMillis], and calls [onTap] when tapped.
 *
 * [onDismissed] fires after the exit animation completes so the host
 * (overlay service) can remove the window.
 */
@Composable
fun DetectionIndicator(
    state: DetectionUiState,
    onTap: () -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = DEFAULT_AUTO_DISMISS_MILLIS,
) {
    var visible by remember(state) { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (!state.isVisible) {
            visible = false
            return@LaunchedEffect
        }
        visible = true
        delay(autoDismissMillis)
        visible = false
        delay(EXIT_ANIMATION_MILLIS.toLong())
        onDismissed()
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 2 },
            exit = fadeOut(tween(EXIT_ANIMATION_MILLIS)) +
                slideOutVertically(tween(EXIT_ANIMATION_MILLIS)) { it / 2 },
        ) {
            DetectionIndicatorChip(state = state, onTap = onTap)
        }
    }
}

/**
 * The chip itself, with no animation or auto-dismiss. Kept separate so
 * previews can render every state statically.
 */
@Composable
fun DetectionIndicatorChip(
    state: DetectionUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val colors = severityColors(state.severity, darkTheme)
    val containerColor = if (darkTheme) Color(0xFF1E2124) else Color.White
    val borderColor = if (darkTheme) Color(0xFF3A3F45) else Color(0xFFE2E5E9)
    val chevronColor = if (darkTheme) Color(0xFF8A9097) else Color(0xFF9AA1A9)

    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 6.dp,
        onClick = onTap,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colors.iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = severityIcon(state.severity),
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = state.message,
                color = colors.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )

            state.riskScore?.let { score ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(colors.accent, CircleShape),
                )
                Text(
                    text = "$score%",
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open details",
                tint = chevronColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
