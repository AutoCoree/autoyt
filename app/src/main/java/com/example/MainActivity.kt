package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.util.NotificationHelper
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.alerts.AlertsCenterScreen
import com.example.ui.analytics.ComparativeAnalyticsScreen
import com.example.ui.api_settings.ApiIntegrationsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.scheduler.SchedulerScreen
import com.example.ui.script_editor.ScriptEditorScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.trends.TrendsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                CreatorFlowApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CreatorFlowApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(AppScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                    Triple(AppScreen.SCRIPT_EDITOR, "Script IA", Icons.Default.AutoAwesome),
                    Triple(AppScreen.TRENDS, "Tendances", Icons.AutoMirrored.Filled.TrendingUp),
                    Triple(AppScreen.SCHEDULER, "Planning", Icons.Default.Schedule),
                    Triple(AppScreen.ANALYTICS, "Comparatif", Icons.Default.BarChart),
                    Triple(AppScreen.API_INTEGRATIONS, "API", Icons.Default.VpnKey)
                )

                navItems.forEach { (screen, label, icon) ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = PrimaryPurple,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                (scaleIn(initialScale = 0.97f, animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(scaleOut(targetScale = 1.03f, animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.SCRIPT_EDITOR -> ScriptEditorScreen(viewModel = viewModel)
                AppScreen.TRENDS -> TrendsScreen(viewModel = viewModel)
                AppScreen.SCHEDULER -> SchedulerScreen(viewModel = viewModel)
                AppScreen.ANALYTICS -> ComparativeAnalyticsScreen(viewModel = viewModel)
                AppScreen.API_INTEGRATIONS -> ApiIntegrationsScreen(viewModel = viewModel)
                AppScreen.ALERTS -> AlertsCenterScreen(viewModel = viewModel)
            }
        }
    }
}
