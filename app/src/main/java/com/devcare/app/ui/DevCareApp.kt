package com.devcare.app.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devcare.app.data.DevCareRepository
import com.devcare.app.ui.screens.*
import com.devcare.app.ui.theme.DevCareTheme

@Composable
fun DevCareApp(
    settingsViewModel: SettingsViewModel,
    repository: DevCareRepository,
    context: Context
) {
    // Observe settings for theme
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    // Check onboarding
    val onboardingCompleted = settings.onboardingCompleted

    DevCareTheme(themeMode = settings.themeMode) {
        if (!onboardingCompleted) {
            val onboardingVm: OnboardingViewModel = viewModel(
                factory = OnboardingViewModel.Factory(repository, context)
            )
            OnboardingScreen(viewModel = onboardingVm)
        } else {
            MainApp(repository = repository, context = context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(repository: DevCareRepository, context: Context) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    data class NavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

    val navItems = listOf(
        NavItem("dashboard", "Dashboard") { Icon(Icons.Default.Dashboard, "Dashboard") },
        NavItem("reminders", "Reminders") { Icon(Icons.Default.Notifications, "Reminders") },
        NavItem("statistics", "Statistics") { Icon(Icons.Default.BarChart, "Statistics") },
        NavItem("settings", "Settings") { Icon(Icons.Default.Settings, "Settings") }
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = item.icon,
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                val vm: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(repository, context)
                )
                DashboardScreen(viewModel = vm)
            }
            composable("reminders") {
                val vm: RemindersViewModel = viewModel(
                    factory = RemindersViewModel.Factory(repository)
                )
                RemindersScreen(viewModel = vm)
            }
            composable("statistics") {
                val vm: StatisticsViewModel = viewModel(
                    factory = StatisticsViewModel.Factory(repository)
                )
                StatisticsScreen(viewModel = vm)
            }
            composable("settings") {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository, context)
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
