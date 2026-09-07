cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/MainAppScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.viewmodel.MainViewModel

@Composable
fun MainAppScreen(
    mainViewModel: MainViewModel,
    rootNavController: NavHostController
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "home_tab",
                    onClick = { bottomNavController.navigate("home_tab") { popUpTo(bottomNavController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == "withdraw_tab",
                    onClick = { bottomNavController.navigate("withdraw_tab") { popUpTo(bottomNavController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Withdraw") },
                    label = { Text("Withdraw") }
                )
                NavigationBarItem(
                    selected = currentRoute == "leaderboard_tab",
                    onClick = { bottomNavController.navigate("leaderboard_tab") { popUpTo(bottomNavController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                    label = { Text("Top 50") }
                )
                NavigationBarItem(
                    selected = currentRoute == "refer_tab",
                    onClick = { bottomNavController.navigate("refer_tab") { popUpTo(bottomNavController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Refer") },
                    label = { Text("Refer") }
                )
                NavigationBarItem(
                    selected = currentRoute == "profile_tab",
                    onClick = { bottomNavController.navigate("profile_tab") { popUpTo(bottomNavController.graph.startDestinationId) { saveState = true } ; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(padding)
        ) {
            composable("home_tab") {
                HomeScreen(
                    viewModel = mainViewModel,
                    onNavigateToTasks = { rootNavController.navigate("tasks") },
                    onNavigateToWallet = { bottomNavController.navigate("withdraw_tab") },
                    onNavigateToDeals = { rootNavController.navigate("deals") },
                    onNavigateToProfile = { bottomNavController.navigate("profile_tab") },
                    onNavigateToSpin = { rootNavController.navigate("spin") },
                    onNavigateToScratch = { rootNavController.navigate("scratch") },
                    onNavigateToVideo = { rootNavController.navigate("watch_video") },
                    onNavigateToRefer = { bottomNavController.navigate("refer_tab") },
                    onNavigateToLeaderboard = { bottomNavController.navigate("leaderboard_tab") }
                )
            }
            composable("withdraw_tab") {
                WalletScreen(
                    viewModel = mainViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onNavigateToHistory = { rootNavController.navigate("history") }
                )
            }
            composable("leaderboard_tab") {
                LeaderboardScreen(viewModel = mainViewModel)
            }
            composable("refer_tab") {
                ReferScreen(viewModel = mainViewModel)
            }
            composable("profile_tab") {
                ProfileScreen(
                    viewModel = mainViewModel,
                    onBack = { bottomNavController.popBackStack() },
                    onNavigateToHistory = { rootNavController.navigate("history") },
                    onLogout = {
                        rootNavController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
INNER_EOF
