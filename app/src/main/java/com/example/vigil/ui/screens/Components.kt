package com.example.vigil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.sentinel.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilPrimaryContainer
import com.example.vigil.ui.theme.VigilPrimaryFixed
import kotlinx.coroutines.delay

/** Wordmark: the Vigil logo (shield + name baked in). */
@Composable
fun VigilWordmark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.vigil_logo),
        contentDescription = "Vigil",
        modifier = modifier.size(40.dp)
    )
}

/** Circular brand emblem — concentric halo + shield check. */
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

/**
 * Fake "screen recording" of a messaging app receiving mean texts, with Vigil's AI
 * sweeping in to flag the last one as a threat. Loops continuously.
 */
@Composable
fun MessagesScanDemo(modifier: Modifier = Modifier) {
    val messages = remember {
        listOf(
            "hey",
            "why do you even bother showing up anymore",
            "nobody actually likes you, you know that right"
        )
    }
    var revealed by remember { mutableStateOf(0) }
    var scanning by remember { mutableStateOf(false) }
    var flagged by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            revealed = 0
            scanning = false
            flagged = false
            delay(500)
            for (i in messages.indices) {
                delay(750)
                revealed = i + 1
            }
            delay(400)
            scanning = true
            delay(1300)
            scanning = false
            flagged = true
            delay(2800)
        }
    }

    val scanProgress by animateFloatAsState(
        targetValue = if (scanning) 1f else 0f,
        animationSpec = tween(durationMillis = 1300, easing = LinearEasing),
        label = "scan-progress"
    )

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
                    Text("Unknown", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Mobile", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    messages.forEachIndexed { i, text ->
                        AnimatedVisibility(
                            visible = i < revealed,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                        ) {
                            val isLast = i == messages.lastIndex
                            Column {
                                Box(
                                    modifier = Modifier
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
                                        fontSize = 13.sp,
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

                if (scanning) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (scanProgress * 168).dp)
                            .background(VigilPrimary)
                    )
                }

                Box(Modifier.align(Alignment.TopEnd)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = flagged,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Row(
                            Modifier
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Threat detected",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Solid primary button with optional trailing arrow. */
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

/** Level-1 card: white surface, 1px outline, no shadow. */
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

/** Feature row: tinted icon tile + title + body. */
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
