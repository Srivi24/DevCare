package com.devcare.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val isActive by viewModel.isActive.collectAsStateWithLifecycle()
    val nextAlarm by viewModel.nextAlarmTime.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.todayStats.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DevCare",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your silent health assistant",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Start/Stop Button
        val buttonColor by animateColorAsState(
            targetValue = if (isActive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            label = "buttonColor"
        )
        val buttonSize by animateDpAsState(
            targetValue = if (isActive) 120.dp else 110.dp,
            label = "buttonSize"
        )

        FilledIconButton(
            onClick = { viewModel.toggleEngine() },
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = buttonColor
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isActive) "Stop" else "Start",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = if (isActive) "STOP" else "START",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Next Reminder",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nextAlarm,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Working Hours
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⏰ Working Hours", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${settings.workingStartTime} — ${settings.workingEndTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Focus Mode Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "\uD83C\uDFAF Focus Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = settings.focusModeEnabled,
                    onCheckedChange = { viewModel.toggleFocusMode() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Stats
        Text(
            text = "Today's Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), label = "Start", value = "${stats.startCount}", emoji = "▶\uFE0F")
            StatCard(modifier = Modifier.weight(1f), label = "Stop", value = "${stats.stopCount}", emoji = "⏹\uFE0F")
            StatCard(modifier = Modifier.weight(1f), label = "Reminders", value = "${stats.reminderTriggers}", emoji = "\uD83D\uDD14")
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    emoji: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
