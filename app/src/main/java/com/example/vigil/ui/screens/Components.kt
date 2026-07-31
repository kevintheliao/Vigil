package com.example.vigil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.vigilapp.vigil.R
import com.example.vigil.detection.DetectionIndicatorChip
import com.example.vigil.detection.DetectionUiState
import com.example.vigil.detection.Severity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilPrimaryContainer
import com.example.vigil.ui.theme.VigilPrimaryFixed
import kotlinx.coroutines.delay

/** Vigil logo  */
@Composable
fun VigilWordmark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.vigil_logo),
        contentDescription = "Vigil",
        modifier = modifier.size(40.dp)
    )
}

@Composable
fun ShieldEmblem(size: Int = 180, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(VigilPrimaryFixed.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((size * 0.42f).dp)
                .background(VigilPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size((size * 0.22f).dp)
            )
        }
    }
}

private class ScanScenario(
    val contactName: String,
    val contactSubtitle: String,
    val messages: List<String>,
    val resultLabel: String,
    val viewportHeight: androidx.compose.ui.unit.Dp,
    val bodyFontSize: androidx.compose.ui.unit.TextUnit,
    val bodyLineHeight: androidx.compose.ui.unit.TextUnit
)

private val scanDemoScenarios = listOf(
    ScanScenario(
        contactName = "Unknown",
        contactSubtitle = "Mobile",
        messages = listOf(
            "hey",
            "why do you even bother showing up anymore",
            "nobody actually likes you, you know that right"
        ),
        resultLabel = "Threat detected",
        viewportHeight = 200.dp,
        bodyFontSize = 13.sp,
        bodyLineHeight = 18.sp
    ),
    ScanScenario(
        contactName = "Scammer",
        contactSubtitle = "Mobile",
        messages = listOf(
            "URGENT: Your package delivery has been suspended due to an incorrect " +
                "shipping address.\n\nTo avoid your package being returned, please " +
                "confirm your information within the next 30 minutes:\n\n" +
                "hxxps://track-delivery-check[.]com\n\nFailure to verify today will " +
                "result in an additional \$4.99 redelivery fee. Reply YES to continue " +
                "or call (555) 123-4567 for assistance."
        ),
        resultLabel = "Scam detected",
        viewportHeight = 320.dp,
        bodyFontSize = 12.sp,
        bodyLineHeight = 17.sp
    )
)

@Composable
fun MessagesScanDemo(modifier: Modifier = Modifier) {
    var scenarioIndex by remember { mutableStateOf(0) }
    var revealed by remember { mutableStateOf(0) }
    var flagged by remember { mutableStateOf(false) }
    var riskScore by remember { mutableStateOf(0) }
    val scenario = scanDemoScenarios[scenarioIndex]
    val viewportHeight by animateDpAsState(
        targetValue = scenario.viewportHeight,
        animationSpec = tween(durationMillis = 500),
        label = "viewport-height"
    )

    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var flaggedBubbleCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var badgeCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(Unit) {
        var idx = 0
        while (true) {
            scenarioIndex = idx
            val messages = scanDemoScenarios[idx].messages
            revealed = 0
            flagged = false
            flaggedBubbleCoords = null
            badgeCoords = null
            delay(500)
            for (i in messages.indices) {
                delay(750)
                revealed = i + 1
            }
            delay(600)
            riskScore = (60..99).random()
            flagged = true
            delay(3200)
            idx = (idx + 1) % scanDemoScenarios.size
        }
    }

    VigilCard(modifier) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier.size(32.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(scenario.contactName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(scenario.contactSubtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(viewportHeight)
                    .padding(16.dp)
                    .onGloballyPositioned { containerCoords = it }
            ) {
                key(scenarioIndex) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        scenario.messages.forEachIndexed { i, text ->
                            AnimatedVisibility(
                                visible = i < revealed,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                            ) {
                                val isLast = i == scenario.messages.lastIndex
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .then(
                                                if (isLast) {
                                                    Modifier.onGloballyPositioned { flaggedBubbleCoords = it }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .background(
                                                if (isLast && flagged) {
                                                    MaterialTheme.colorScheme.errorContainer
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceContainer
                                                },
                                                RoundedCornerShape(14.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text,
                                            fontSize = scenario.bodyFontSize,
                                            lineHeight = scenario.bodyLineHeight,
                                            color = if (isLast && flagged) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                val container = containerCoords
                val bubble = flaggedBubbleCoords
                val badge = badgeCoords
                if (flagged && container?.isAttached == true && bubble?.isAttached == true && badge?.isAttached == true) {
                    Canvas(Modifier.fillMaxSize()) {
                        val start = container.localPositionOf(badge, Offset(badge.size.width / 2f, badge.size.height.toFloat()))
                        val end = container.localPositionOf(bubble, Offset(bubble.size.width.toFloat(), 0f))

                        // Right-angle elbow connector routed down the right margin, clear of the bubbles.
                        val rightEdge = size.width - 8.dp.toPx()
                        val cornerX = maxOf(start.x, end.x).coerceAtMost(rightEdge)
                        val cornerRadius = 10.dp.toPx()
                            .coerceAtMost(kotlin.math.abs(end.y - start.y) / 2f)
                            .coerceAtMost(kotlin.math.abs(cornerX - start.x) / 2f)
                            .coerceAtMost(kotlin.math.abs(cornerX - end.x) / 2f)
                            .coerceAtLeast(0f)

                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            lineTo(cornerX - cornerRadius, start.y)
                            quadraticTo(cornerX, start.y, cornerX, start.y + cornerRadius * (if (end.y > start.y) 1f else -1f))
                            lineTo(cornerX, end.y - cornerRadius * (if (end.y > start.y) 1f else -1f))
                            quadraticTo(cornerX, end.y, cornerX - cornerRadius, end.y)
                            lineTo(end.x, end.y)
                        }
                        drawPath(
                            path = path,
                            color = VigilPrimary,
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        )
                    }
                }

                Box(Modifier.align(Alignment.TopEnd)) {
                    key(scenarioIndex) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = flagged,
                            enter = scaleIn() + fadeIn()
                        ) {
                            DetectionIndicatorChip(
                                state = DetectionUiState(
                                    severity = Severity.HIGH,
                                    message = scenario.resultLabel,
                                    riskScore = riskScore
                                ),
                                onTap = {},
                                modifier = Modifier.onGloballyPositioned { badgeCoords = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VigilPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VigilPrimary)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (showArrow) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun VigilCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun FeatureRow(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    VigilCard(modifier = modifier) {
        Row(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(VigilPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VigilPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(body, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

/** Step indicator dots for the onboarding flow. */
@Composable
fun OnboardingProgressDots(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        repeat(total) { index ->
            Box(
                Modifier
                    .size(if (index == current) 8.dp else 6.dp)
                    .background(
                        if (index == current) VigilPrimary else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    )
            )
        }
    }
}

/** Looping mock of the Android Settings toggle row, so onboarding shows what "enabling" looks like. */
@Composable
fun SettingsToggleDemo(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    var checked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(700)
        checked = true
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            "Settings",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = VigilPrimary, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedTrackColor = VigilPrimary)
            )
        }
    }
}
