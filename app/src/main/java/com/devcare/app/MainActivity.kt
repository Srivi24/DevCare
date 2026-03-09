package com.devcare.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.devcare.app.data.AppDatabase
import com.devcare.app.data.DevCareRepository
import com.devcare.app.ui.DevCareApp
import com.devcare.app.ui.screens.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle notification permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val repository = DevCareRepository(
            db.reminderDao(),
            db.statisticsDao(),
            db.settingsDao()
        )

        // Use a ViewModel to track if settings are loaded
        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModel.Factory(repository, applicationContext)
        }

        // Keep the splash screen on-screen until the first settings emission
        // This avoids flickering from Onboarding to Dashboard
        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isLoaded.value == false
        }

        setContent {
            DevCareApp(settingsViewModel = settingsViewModel, repository = repository, context = applicationContext)
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        // Post Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Exact Alarm (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }
}
