# Fix trailing brackets in MainActivity
cat << 'INNER_EOF' > app/src/main/java/com/example/MainActivity.kt
package com.example
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MathCaptchaScreen
import com.example.ui.screens.ShoppingDealsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.ReferScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.screens.ScratchCardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.ads.AdsManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    AdsManager.initialize(this)
    setContent {
        PocketCashApp()
    }
  }
}

@Composable
fun PocketCashApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    
    MyApplicationTheme(darkTheme = isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(
                        onNavigateNext = { navController.navigate("auth") { popUpTo("splash") { inclusive = true } } }
                    )
                }
                composable("auth") {
                    AuthScreen(
                        viewModel = mainViewModel,
                        onNavigateToHome = { navController.navigate("home") { popUpTo("auth") { inclusive = true } } }
                    )
                }
                composable("home") {
                    HomeScreen(
                        viewModel = mainViewModel,
                        onNavigateToTasks = { navController.navigate("tasks") },
                        onNavigateToWallet = { navController.navigate("wallet") },
                        onNavigateToDeals = { navController.navigate("deals") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSpin = { navController.navigate("spin") },
                        onNavigateToScratch = { navController.navigate("scratch") },
                        onNavigateToVideo = { navController.navigate("watch_video") },
                        onNavigateToRefer = { navController.navigate("refer") },
                        onNavigateToLeaderboard = { navController.navigate("leaderboard") }
                    )
                }
                composable("watch_video") {
                    com.example.ui.screens.WatchVideoScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("tasks") {
                    MathCaptchaScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("wallet") {
                    WalletScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToHistory = { navController.navigate("history") }
                    )
                }
                composable("deals") {
                    ShoppingDealsScreen(onBack = { navController.popBackStack() })
                }
                composable("profile") {
                    ProfileScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToHistory = { navController.navigate("history") }
                    )
                }
                composable("history") {
                    TransactionHistoryScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("spin") {
                    SpinWheelScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("refer") {
                    ReferScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("leaderboard") {
                    LeaderboardScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("scratch") {
                    ScratchCardScreen(
                        viewModel = mainViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
INNER_EOF
