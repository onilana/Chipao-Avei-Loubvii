package com.pnba

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.pnba.util.AppNotificationManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.pnba.ui.HymnalViewModel
import com.pnba.ui.Screen
import com.pnba.ui.screens.AboutScreen
import com.pnba.ui.screens.HomeScreen
import com.pnba.ui.screens.HymnDetailScreen
import com.pnba.ui.screens.IndexScreen
import com.pnba.ui.screens.SearchScreen
import com.pnba.ui.theme.SanctuaryHymnalTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HymnalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppNotificationManager.createNotificationChannels(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        viewModel.checkForAppUpdates(this)

        setContent {
            val readingTheme by viewModel.readingTheme.collectAsState()
            val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            // Handle Keep Screen Awake flag on the Window:
            // Follows system screen timeout setting by default across the app.
            // When user enables "Keep Screen Awake", it only prevents sleep while reading a hymn (DETAIL).
            LaunchedEffect(keepScreenAwake, currentScreen) {
                if (keepScreenAwake && currentScreen == Screen.DETAIL) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            SanctuaryHymnalTheme(readingTheme = readingTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    bottomBar = {
                        // Display bottom navigation only on primary browsing tabs
                        if (currentScreen != Screen.DETAIL) {
                            HymnalBottomNavigation(
                                currentScreen = currentScreen,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                Screen.HOME -> HomeScreen(viewModel = viewModel)
                                Screen.INDEX -> IndexScreen(viewModel = viewModel)
                                Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                                Screen.DETAIL -> HymnDetailScreen(viewModel = viewModel)
                                Screen.ABOUT -> AboutScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HymnalBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("main_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        val navItems = listOf(
            Triple(Screen.HOME, "Home", Icons.Filled.Home to Icons.Outlined.Home),
            Triple(Screen.INDEX, "Index", Icons.Filled.GridView to Icons.Outlined.GridView),
            Triple(Screen.SEARCH, "Search", Icons.Filled.Search to Icons.Outlined.Search),
            Triple(Screen.ABOUT, "About", Icons.Filled.Info to Icons.Outlined.Info)
        )

        for ((screen, label, icons) in navItems) {
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase()}")
            )
        }
    }
}
