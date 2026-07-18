package com.example.vigil.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.ui.theme.VigilOnPrimary
import com.example.vigil.ui.theme.VigilPrimaryContainer
import com.example.vigil.ui.theme.VigilTheme

private data class ThreatTopic(
    val icon: ImageVector,
    val chip: String,
    val title: String,
    val description: String,
    val example: String,
    val tips: List<String>
)

private val topics = listOf(
    ThreatTopic(
        Icons.Filled.Warning, "Safety Guide", "Cyberbullying",
        "Persistent negative or aggressive interactions targeting an individual via digital platforms.",
        "\"Publicly sharing private photos or making threats in group chats.\"",
        listOf("Don't retaliate; document the evidence.", "Use platform blocking and reporting tools.")
    ),
    ThreatTopic(
        Icons.Filled.Warning, "High Alert", "Scams & Phishing",
        "Deceptive attempts to obtain sensitive information like passwords or credit card details.",
        "\"Urgent emails asking to verify account details via a suspicious link.\"",
        listOf("Check the sender's actual email address.", "Enable multi-factor authentication (MFA).")
    ),
    ThreatTopic(
        Icons.Filled.Warning, "Community Standards", "Harassment",
        "Repeated, uninvited communication that causes distress or creates a hostile environment.",
        "\"Constant unwanted messages despite being asked to stop.\"",
        listOf("Set clear boundaries early.", "Inform a trusted authority or platform moderator.")
    ),
    ThreatTopic(
        Icons.Filled.Warning, "Ethics", "Abusive Language",
        "Use of profanity, hate speech, or derogatory terms intended to demean or dehumanize.",
        "\"Slurs or targeted insults based on identity or characteristics.\"",
        listOf("Use filter keywords on your social feeds.", "Disengage immediately to de-escalate.")
    ),
)

/** Education tab — threat guide feed. */
@Composable
fun EducationScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VigilWordmark()
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.fillMaxWidth().background(VigilPrimaryContainer, RoundedCornerShape(16.dp)).padding(20.dp)
        ) {
            Column {
                Text("Stay Informed, Stay Secure", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VigilOnPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Explore our comprehensive guide to recognizing and defusing online threats. Education is your first line of defense.",
                    fontSize = 14.sp, color = VigilOnPrimary.copy(alpha = 0.9f), lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        topics.forEach { topic ->
            ThreatCard(topic)
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ThreatCard(topic: ThreatTopic) {
    VigilCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier.size(48.dp).background(VigilPrimaryContainer.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(topic.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Box(
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(100.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(topic.chip, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(topic.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(topic.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp)).padding(12.dp)
            ) {
                Column {
                    Text("EXAMPLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(topic.example, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))
            topic.tips.forEach { tip ->
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(tip, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun EducationPreview() {
    VigilTheme { EducationScreen() }
}
