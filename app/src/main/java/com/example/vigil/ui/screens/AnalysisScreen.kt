package com.example.vigil.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ForwardToInbox
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SpeakerNotesOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.vigil.detection.MessageScorer
import com.example.vigil.detection.Severity
import com.example.vigil.detection.severityColors
import com.example.vigil.detection.severityIcon
import com.example.vigil.ui.theme.VigilTheme

/** What the analysis screen needs to render one flagged message. */
data class AnalysisArgs(
    val severity: Severity,
    val verdict: String,
    val riskScore: Int?,
    val body: String,
)

/** Full-screen breakdown of a flagged message: verdict, message text, matched signals, guidance. */
@Composable
fun AnalysisScreen(args: AnalysisArgs, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = severityColors(args.severity, isSystemInDarkTheme())
    //rule-based signals give the "why"; the ML verdict stands even when none match
    val matchedSignals = remember(args.body) { MessageScorer.score(args.body).matchedSignals }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Analysis", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).background(colors.iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(severityIcon(args.severity), contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(args.verdict, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                args.riskScore?.let {
                    Text("$it% confidence", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("Message")
        VigilCard {
            Text(
                args.body.ifBlank { "(message unavailable)" },
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("Why this was flagged")
        VigilCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (matchedSignals.isEmpty()) {
                    Text(
                        "Vigil's on-device AI recognized patterns in this message that closely match known ${if (args.verdict.contains("scam", true)) "scam" else "harassment"} messages.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    matchedSignals.forEach { signal ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).background(colors.accent, CircleShape))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                signal.replaceFirstChar { it.uppercase() },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("What to do")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            guidance(args.verdict).forEach { (icon, title, detail) ->
                FeatureRow(icon, title, detail)
            }
        }
        Spacer(Modifier.height(24.dp))

        val context = LocalContext.current
        SectionLabel("Get help")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (args.verdict.contains("scam", ignoreCase = true)) {
                FeatureRow(
                    Icons.AutoMirrored.Filled.ForwardToInbox,
                    "Forward to 7726 (SPAM)",
                    "Free reporting line run by all major carriers. Tap to forward this message.",
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, "smsto:7726".toUri())
                                .putExtra("sms_body", args.body)
                                .putExtra(Intent.EXTRA_TEXT, args.body)
                        )
                    },
                )
                FeatureRow(
                    Icons.Filled.Phone,
                    "Report to the FTC",
                    "1-877-382-4357 (877-FTC-HELP), or online at reportfraud.ftc.gov. Tap to call.",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:18773824357".toUri()))
                    },
                )
            } else {
                FeatureRow(
                    Icons.Filled.Phone,
                    "988 Suicide & Crisis Lifeline",
                    "Free, confidential, 24/7 support if messages like this are weighing on you. Tap to call 988.",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:988".toUri()))
                    },
                )
                FeatureRow(
                    Icons.Filled.Sms,
                    "Crisis Text Line",
                    "Text HOME to 741741 to reach a trained counselor, 24/7. Tap to start a text.",
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, "smsto:741741".toUri())
                                .putExtra("sms_body", "HOME")
                                .putExtra(Intent.EXTRA_TEXT, "HOME")
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
}

private fun guidance(verdict: String): List<Triple<ImageVector, String, String>> =
    if (verdict.contains("scam", ignoreCase = true)) {
        listOf(
            Triple(Icons.Filled.LinkOff, "Don't tap any links", "Links in scam texts lead to fake sites that steal passwords or payment details."),
            Triple(Icons.Filled.SpeakerNotesOff, "Don't reply", "Any response confirms your number is active and invites more scams."),
            Triple(Icons.Filled.Block, "Block the sender", "Open the conversation menu in your messaging app and block the number."),
        )
    } else {
        listOf(
            Triple(Icons.Filled.DoNotTouch, "Don't engage", "Responding usually escalates harassment. You don't owe a reply."),
            Triple(Icons.Filled.CameraAlt, "Keep evidence", "Screenshot the messages with dates visible in case you need to report them."),
            Triple(Icons.Filled.Block, "Block the sender", "Open the conversation menu in your messaging app and block the number."),
        )
    }

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun AnalysisPreview() {
    VigilTheme {
        AnalysisScreen(
            args = AnalysisArgs(
                severity = Severity.HIGH,
                verdict = "Possible scam",
                riskScore = 99,
                body = "URGENT: Your bank account is suspended. Verify now at http://bit.ly/x2f or lose access",
            ),
            onBack = {},
        )
    }
}
