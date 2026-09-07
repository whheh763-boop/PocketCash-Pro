cat << 'INNER_EOF' > app/src/main/java/com/example/MainActivity.kt
package com.example

import android.os.Bundle
import android.widget.Toast
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
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.MathCaptchaScreen
import com.example.ui.screens.ShoppingDealsScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.screens.ScratchCardScreen
import com.example.ui.screens.WatchVideoScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.ads.AdsManager
import com.example.utils.SecurityManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    
    if (SecurityManager.isVpnActive(this) || !SecurityManager.isDeviceSecure()) {
        Toast.makeText(this, "Anti-Cheat: VPN or Unauthorized Device Detected. Access Denied.", Toast.LENGTH_LONG).show()
        finish()
        return
    }

    enableEdgeToEdge()
    AdsManager.initialize(this)
    setContent {
        PocketCashApp()
    }
  }

  override fun onResume() {
      super.onResume()
      AdsManager.showAdIfAvailable(this)
  }
}

@Composable
fun PocketCashApp() {
    val rootNavController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val isDarkMode by mainViewModel.isDarkMode.collectAsState()
    
    MyApplicationTheme(darkTheme = isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = rootNavController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen(
                        onNavigateNext = { rootNavController.navigate("auth") { popUpTo("splash") { inclusive = true } } }
                    )
                }
                composable("auth") {
                    AuthScreen(
                        viewModel = mainViewModel,
                        onNavigateToHome = { rootNavController.navigate("main") { popUpTo("auth") { inclusive = true } } }
                    )
                }
                composable("main") {
                    MainAppScreen(mainViewModel, rootNavController)
                }
                composable("watch_video") {
                    WatchVideoScreen(
                        viewModel = mainViewModel,
                        onBack = { rootNavController.popBackStack() }
                    )
                }
                composable("tasks") {
                    MathCaptchaScreen(
                        viewModel = mainViewModel,
                        onBack = { rootNavController.popBackStack() }
                    )
                }
                composable("deals") {
                    ShoppingDealsScreen(onBack = { rootNavController.popBackStack() })
                }
                composable("history") {
                    TransactionHistoryScreen(
                        viewModel = mainViewModel,
                        onBack = { rootNavController.popBackStack() }
                    )
                }
                composable("spin") {
                    SpinWheelScreen(
                        viewModel = mainViewModel,
                        onBack = { rootNavController.popBackStack() }
                    )
                }
                composable("scratch") {
                    ScratchCardScreen(
                        viewModel = mainViewModel,
                        onBack = { rootNavController.popBackStack() }
                    )
                }
            }
        }
    }
}
INNER_EOF
