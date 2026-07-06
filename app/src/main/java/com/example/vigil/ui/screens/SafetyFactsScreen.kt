package com.example.vigil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vigil.ui.theme.VigilPrimary
import com.example.vigil.ui.theme.VigilTheme

/** Onboarding stat slide — "Did you know?". */
@Composable
fun SafetyFactsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Did you know?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))

        VigilCard {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("1 in 3", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = VigilPrimary)
                Spacer(Modifier.height(12.dp))
                Text(
                    "teens have been cyberbullied at some point in their lives.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard("$470 Million", "in annual losses to text scams", Modifier.weight(1f))
            StatCard("46%", "of teens have experienced cyberbullying", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Vigil can help",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Vigil watches for scams and bullying in real time, so you hear about " +
                "harmful messages the moment they arrive.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    VigilCard(modifier) {
        Column(
            Modifier.padding(16.dp).height(96.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = VigilPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Citation footer for the stats above — rendered by the onboarding scaffold
 *  just above the Continue button. */
@Composable
fun SafetyFactsSources(modifier: Modifier = Modifier) {
    Text(
        "Sources: UNICEF (2019) · FTC (2025) · Pew Research Center (2022)",
        modifier = modifier,
        fontSize = 10.sp,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F9FB)
@Composable
private fun SafetyFactsPreview() {
    VigilTheme { SafetyFactsScreen() }
}
