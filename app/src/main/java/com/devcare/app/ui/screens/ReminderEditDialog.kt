package com.devcare.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devcare.app.data.model.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditDialog(
    reminder: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val context = LocalContext.current
    val isEditing = reminder != null
    var name by remember { mutableStateOf(reminder?.name ?: "") }
    var emoji by remember { mutableStateOf(reminder?.emoji ?: "\uD83D\uDCA7") }
    var intervalMinutes by remember { mutableStateOf(reminder?.intervalMinutes?.toString() ?: "30") }
    var specialCycle by remember { mutableStateOf(reminder?.specialCycle?.toString() ?: "0") }
    var breakDuration by remember { mutableStateOf(reminder?.specialBreakDurationMinutes?.toString() ?: "0") }
    
    // Tone state
    var selectedToneUri by remember { mutableStateOf(reminder?.tone ?: "default") }
    var selectedToneName by remember { 
        mutableStateOf(if (selectedToneUri == "default") "Default" else "Custom Tone") 
    }

    // Launch system ringtone picker
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                selectedToneUri = uri.toString()
                val ringtone = RingtoneManager.getRingtone(context, uri)
                selectedToneName = ringtone.getTitle(context) ?: "Custom Tone"
            }
        }
    }

    // Common emoji options
    val emojiOptions = listOf(
        "\uD83D\uDCA7", // 💧
        "\uD83E\uDDD8", // 🧘
        "\uD83D\uDC40", // 👀
        "\uD83C\uDF45", // 🍅
        "\uD83C\uDFC3", // 🏃
        "\u2615",       // ☕
        "\uD83E\uDDE0", // 🧠
        "\uD83D\uDCA4", // 💤
        "\uD83C\uDF3F"  // 🌿
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Reminder" else "New Reminder") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Emoji picker
                Column {
                    Text("Emoji", style = MaterialTheme.typography.labelMedium)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        emojiOptions.forEach { e ->
                            FilterChip(
                                selected = emoji == e,
                                onClick = { emoji = e },
                                label = { Text(e) }
                            )
                        }
                    }
                }

                // Tone Picker (Mandatory)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Notification Tone", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = selectedToneName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Tone")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, 
                                        if (selectedToneUri == "default") null else Uri.parse(selectedToneUri))
                                }
                                ringtonePickerLauncher.launch(intent)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Choose Tone")
                        }
                        
                        if (selectedToneUri == "default") {
                            Text(
                                "Please select a custom tone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Interval
                OutlinedTextField(
                    value = intervalMinutes,
                    onValueChange = { intervalMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Interval (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Special cycle
                OutlinedTextField(
                    value = specialCycle,
                    onValueChange = { specialCycle = it.filter { c -> c.isDigit() } },
                    label = { Text("Special cycle (0 = none)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Break duration
                OutlinedTextField(
                    value = breakDuration,
                    onValueChange = { breakDuration = it.filter { c -> c.isDigit() } },
                    label = { Text("Break duration (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val isToneSelected = selectedToneUri != "default"
            TextButton(
                enabled = name.isNotBlank() && isToneSelected && (intervalMinutes.toIntOrNull() ?: 0) > 0,
                onClick = {
                    val interval = intervalMinutes.toIntOrNull() ?: 30
                    val cycle = specialCycle.toIntOrNull() ?: 0
                    val breakDur = breakDuration.toIntOrNull() ?: 0

                    val newReminder = (reminder ?: Reminder(name = "", emoji = "", intervalMinutes = 30)).copy(
                        name = name.trim(),
                        emoji = emoji,
                        intervalMinutes = interval,
                        specialCycle = cycle,
                        specialBreakDurationMinutes = breakDur,
                        tone = selectedToneUri
                    )
                    onSave(newReminder)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
