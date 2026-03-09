package com.devcare.app.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // ── Working Hours ──
        Text(
            text = "Working Hours",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                TimePickerRow(
                    label = "Start Time",
                    value = settings.workingStartTime,
                    context = context,
                    onTimeSelected = { viewModel.updateWorkingStartTime(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                TimePickerRow(
                    label = "End Time",
                    value = settings.workingEndTime,
                    context = context,
                    onTimeSelected = { viewModel.updateWorkingEndTime(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Theme ──
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                val themeOptions = listOf("light" to "☀\uFE0F Light", "dark" to "\uD83C\uDF19 Dark", "system" to "\uD83D\uDCF1 System")
                themeOptions.forEachIndexed { index, (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateThemeMode(value) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        RadioButton(
                            selected = settings.themeMode == value,
                            onClick = { viewModel.updateThemeMode(value) }
                        )
                    }
                    if (index < themeOptions.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Toggles ──
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                ToggleRow(
                    label = "Snooze Feature",
                    checked = settings.snoozeEnabled,
                    onToggle = { viewModel.toggleSnooze() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ToggleRow(
                    label = "Action Buttons",
                    checked = settings.actionButtonsEnabled,
                    onToggle = { viewModel.toggleActionButtons() }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Battery Optimization ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.openBatteryOptimization() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery Optimization",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Disable to ensure reliable alarms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    value: String,
    context: Context,
    onTimeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val parts = value.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                TimePickerDialog(context, { _, h, m ->
                    onTimeSelected(String.format("%02d:%02d", h, m))
                }, hour, minute, true).show()
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
