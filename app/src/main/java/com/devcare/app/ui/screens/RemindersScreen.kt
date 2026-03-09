package com.devcare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devcare.app.data.model.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingReminder = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reminders yet.\nTap + to add one!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onEdit = {
                                editingReminder = reminder
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        ReminderEditDialog(
            reminder = editingReminder,
            onDismiss = { showDialog = false },
            onSave = { reminder ->
                if (editingReminder != null) {
                    viewModel.updateReminder(reminder)
                } else {
                    viewModel.insertReminder(reminder)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(text = reminder.emoji, fontSize = 32.sp)

            Spacer(modifier = Modifier.width(12.dp))

            // Name + interval
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Every ${reminder.intervalMinutes} min" +
                            if (reminder.specialCycle > 0) " • Break every ${reminder.specialCycle} cycles" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Toggle
            Switch(checked = reminder.enabled, onCheckedChange = { onToggle() })

            // Edit
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Delete
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
