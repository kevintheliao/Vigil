package com.example.vigil.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.vigil.ui.theme.VigilOnPrimary
import com.example.vigil.ui.theme.VigilPrimaryContainer
import com.example.vigil.ui.theme.VigilTheme

private data class Hotline(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val uri: String,
    val smsBody: String? = null
)

private data class ThreatTopic(
    val icon: ImageVector,
    val chip: String,
    val title: String,
    val description: String,
    val example: String,
    val warningSigns: List<String>,
    val ifItHappensToYou: List<String>,
    val tips: List<String>,
    val sources: List<String>,
    val hotlines: List<Hotline>
)

private val crisisTextLine = Hotline(
    Icons.Filled.Sms, "Crisis Text Line", "Text HOME to 741741 to reach a trained counselor, 24/7. Tap to start a text.",
    "smsto:741741", smsBody = "HOME"
)

private val topics = listOf(
    ThreatTopic(
        Icons.Filled.Warning, "Safety Guide", "Cyberbullying",
        "Persistent negative or aggressive interactions targeting an individual via digital platforms.",
        "\"Publicly sharing private photos or making threats in group chats.\"",
        listOf(
            "Repeated mean or threatening messages from the same person or group.",
            "Embarrassing photos or rumors spread without your consent.",
            "Being deliberately excluded from group chats to isolate or humiliate you."
        ),
        listOf(
            "Don't respond or retaliate — it rarely helps and can escalate things.",
            "Screenshot the messages, including usernames and timestamps, before blocking.",
            "Block the person and report the account on the platform.",
            "Tell a parent, teacher, or trusted adult what's happening — you shouldn't handle it alone.",
            "If you feel unsafe or threatened, this can be reported to local law enforcement."
        ),
        listOf("Don't retaliate; document the evidence.", "Use platform blocking and reporting tools."),
        listOf("StopBullying.gov (U.S. Dept. of Health & Human Services)", "Cyberbullying Research Center", "Pew Research Center (2022)"),
        listOf(
            crisisTextLine,
            Hotline(Icons.Filled.Phone, "Childhelp National Child Abuse Hotline", "1-800-422-4453, 24/7. Tap to call.", "tel:18004224453")
        )
    ),
    ThreatTopic(
        Icons.Filled.Warning, "High Alert", "Scams & Phishing",
        "Deceptive attempts to obtain sensitive information like passwords or credit card details.",
        "\"Urgent emails asking to verify account details via a suspicious link.\"",
        listOf(
            "Urgent language pressuring you to act immediately (\"account will be suspended\").",
            "Links or attachments from senders you don't recognize.",
            "Requests for passwords, codes, or payment that a real company wouldn't ask for over text."
        ),
        listOf(
            "Don't click any links or download attachments in the message.",
            "Never enter your password, PIN, or a verification code sent to you.",
            "Verify by contacting the company directly through its official app or website — not a number or link from the message.",
            "Report the message as spam/phishing and delete it.",
            "If you already clicked a link or shared info, change the affected password immediately and enable multi-factor authentication."
        ),
        listOf("Check the sender's actual email address.", "Enable multi-factor authentication (MFA)."),
        listOf("Federal Trade Commission (FTC.gov)", "CISA (Cybersecurity & Infrastructure Security Agency)", "FBI Internet Crime Complaint Center (IC3)"),
        listOf(
            Hotline(Icons.Filled.Phone, "FTC Consumer Response Center", "1-877-382-4357 (877-FTC-HELP). Tap to call.", "tel:18773824357"),
            Hotline(Icons.Filled.Phone, "National Elder Fraud Hotline (DOJ)", "1-833-372-8311, for scam victims of any age. Tap to call.", "tel:18333728311")
        )
    ),
    ThreatTopic(
        Icons.Filled.Warning, "Community Standards", "Harassment",
        "Repeated, uninvited communication that causes distress or creates a hostile environment.",
        "\"Constant unwanted messages despite being asked to stop.\"",
        listOf(
            "Messages continue after you've asked the person to stop.",
            "Contact comes from multiple accounts or numbers to get around blocks.",
            "The messages make you feel watched, pressured, or afraid."
        ),
        listOf(
            "State clearly, once, that you want the contact to stop — then stop engaging.",
            "Block the sender on every platform and number they've used.",
            "Save evidence: screenshots, dates, and any related accounts.",
            "Report the harassment to the platform and to a trusted adult or authority figure.",
            "If it involves threats to your safety, contact local law enforcement."
        ),
        listOf("Set clear boundaries early.", "Inform a trusted authority or platform moderator."),
        listOf("RAINN (Rape, Abuse & Incest National Network)", "StopBullying.gov", "Pew Research Center (2021)"),
        listOf(
            Hotline(Icons.Filled.Phone, "National Domestic Violence Hotline", "1-800-799-7233, 24/7. Tap to call.", "tel:18007997233"),
            crisisTextLine
        )
    ),
    ThreatTopic(
        Icons.Filled.Warning, "Ethics", "Abusive Language",
        "Use of profanity, hate speech, or derogatory terms intended to demean or dehumanize.",
        "\"Slurs or targeted insults based on identity or characteristics.\"",
        listOf(
            "Slurs or insults targeting your identity, background, or appearance.",
            "Language meant to provoke, humiliate, or shut down a conversation.",
            "A pattern that escalates the longer you stay engaged."
        ),
        listOf(
            "Disengage immediately — don't try to argue or reason with abusive language.",
            "Mute or filter keywords so future messages don't reach you the same way.",
            "Report the message on the platform it came from.",
            "Talk to someone you trust if the language affected you — it's okay for words to sting.",
        ),
        listOf("Use filter keywords on your social feeds.", "Disengage immediately to de-escalate."),
        listOf("Anti-Defamation League (ADL)", "StopBullying.gov"),
        listOf(
            Hotline(Icons.Filled.Phone, "988 Suicide & Crisis Lifeline", "Free, confidential, 24/7 support. Tap to call 988.", "tel:988"),
            crisisTextLine
        )
    ),
)

/** Education tab — threat guide feed with per-topic lesson screens. */
@Composable
fun EducationScreen(modifier: Modifier = Modifier) {
    var selectedTopic by remember { mutableStateOf<ThreatTopic?>(null) }
    val topic = selectedTopic

    if (topic != null) {
        BackHandler(onBack = { selectedTopic = null })
    }

    AnimatedContent(
        targetState = topic,
        transitionSpec = {
            if (targetState != null) {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) + fadeIn(tween(280)) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) + fadeOut(tween(180))
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) + fadeIn(tween(280)) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) + fadeOut(tween(180))
            }
        },
        label = "education-lesson-transition",
    ) { shownTopic ->
        if (shownTopic != null) {
            LessonScreen(shownTopic, onBack = { selectedTopic = null }, modifier = modifier)
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                topics.forEach { t ->
                    ThreatCard(t, onClick = { selectedTopic = t })
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ThreatCard(topic: ThreatTopic, onClick: () -> Unit) {
    VigilCard(modifier = Modifier.clickable(onClick = onClick)) {
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
            Text("Tap to learn more", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Full lesson for one threat topic: what it is, warning signs, what to do, and prevention tips. */
@Composable
private fun LessonScreen(topic: ThreatTopic, onBack: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        item { Spacer(Modifier.height(12.dp)) }

        stickyHeader {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(topic.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        item {
        Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).background(VigilPrimaryContainer.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(topic.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(topic.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("Example")
        Box(
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp)).padding(12.dp)
        ) {
            Text(topic.example, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("Warning Signs")
        VigilCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topic.warningSigns.forEach { sign ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(sign, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("If This Happens To You")
        VigilCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topic.ifItHappensToYou.forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            Modifier.size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(step, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        val context = LocalContext.current
        SectionLabel("Get Help")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            topic.hotlines.forEach { hotline ->
                FeatureRow(
                    hotline.icon, hotline.title, hotline.detail,
                    modifier = Modifier.clickable {
                        val intent = if (hotline.smsBody != null) {
                            Intent(Intent.ACTION_SENDTO, hotline.uri.toUri())
                                .putExtra("sms_body", hotline.smsBody)
                                .putExtra(Intent.EXTRA_TEXT, hotline.smsBody)
                        } else {
                            Intent(Intent.ACTION_DIAL, hotline.uri.toUri())
                        }
                        context.startActivity(intent)
                    },
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        SectionLabel("Prevention Tips")
        VigilCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                topic.tips.forEach { tip ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(tip, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Sources: " + topic.sources.joinToString(" · "),
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.fillMaxWidth()
                .background(VigilPrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Vigil scans your messages on-device and flags threats like this automatically.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun EducationPreview() {
    VigilTheme { EducationScreen() }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun LessonPreview() {
    VigilTheme { LessonScreen(topics[0], onBack = {}) }
}
