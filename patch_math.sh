cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/MathCaptchaScreen.kt
package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdsManager
import com.example.viewmodel.MainViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathCaptchaScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Math Quiz", "Captcha Solver")
    val userState by viewModel.userState.collectAsState()
    val animatedCoins by animateIntAsState(
        targetValue = userState.coinBalance,
        animationSpec = tween(1000),
        label = "coinAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = "$animatedCoins Coins",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (selectedTabIndex == 0) {
                    MathQuizTab(viewModel)
                } else {
                    CaptchaTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun MathQuizTab(viewModel: MainViewModel) {
    val userState by viewModel.userState.collectAsState()
    var num1 by remember { mutableIntStateOf(Random.nextInt(1, 50)) }
    var num2 by remember { mutableIntStateOf(Random.nextInt(1, 50)) }
    var answerInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Attempts left today: ${userState.dailyMathLimit}/15", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$num1 + $num2 = ?",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = answerInput,
            onValueChange = { answerInput = it },
            label = { Text("Your Answer") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (answerInput.toIntOrNull() == (num1 + num2)) {
                    isLoading = true
                    viewModel.useMathQuizAttempt(
                        onSuccess = {
                            activity?.let {
                                AdsManager.showRewardedAd(
                                    activity = it,
                                    onRewardEarned = {
                                        viewModel.addCoins(10, "Math Quiz Reward")
                                        feedbackMessage = "Correct! +10 Coins"
                                    },
                                    onAdDismissed = {
                                        answerInput = ""
                                        num1 = Random.nextInt(1, 50)
                                        num2 = Random.nextInt(1, 50)
                                        isLoading = false
                                    }
                                )
                            } ?: run {
                                viewModel.addCoins(10, "Math Quiz Reward")
                                feedbackMessage = "Correct! +10 Coins (No Ad shown)"
                                answerInput = ""
                                num1 = Random.nextInt(1, 50)
                                num2 = Random.nextInt(1, 50)
                                isLoading = false
                            }
                        },
                        onFail = {
                            feedbackMessage = "Daily limit reached!"
                            isLoading = false
                        }
                    )
                } else {
                    feedbackMessage = "Incorrect! Try again."
                }
            },
            enabled = !isLoading && userState.dailyMathLimit > 0
        ) {
            Text(if (isLoading) "Loading Ad..." else "Submit Answer")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(feedbackMessage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CaptchaTab(viewModel: MainViewModel) {
    val userState by viewModel.userState.collectAsState()
    fun generateCaptcha() = List(6) { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
    var currentCaptcha by remember { mutableStateOf(generateCaptcha()) }
    var answerInput by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Attempts left today: ${userState.dailyCaptchaLimit}/20", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.padding(16.dp)) {
            Text(
                text = currentCaptcha,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(32.dp),
                letterSpacing = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = answerInput,
            onValueChange = { answerInput = it.uppercase() },
            label = { Text("Enter Captcha") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (answerInput == currentCaptcha) {
                    isLoading = true
                    viewModel.useCaptchaAttempt(
                        onSuccess = {
                            activity?.let {
                                AdsManager.showRewardedAd(
                                    activity = it,
                                    onRewardEarned = {
                                        viewModel.addCoins(5, "Captcha Reward")
                                        feedbackMessage = "Correct! +5 Coins"
                                    },
                                    onAdDismissed = {
                                        answerInput = ""
                                        currentCaptcha = generateCaptcha()
                                        isLoading = false
                                    }
                                )
                            } ?: run {
                                viewModel.addCoins(5, "Captcha Reward")
                                feedbackMessage = "Correct! +5 Coins (No Ad shown)"
                                answerInput = ""
                                currentCaptcha = generateCaptcha()
                                isLoading = false
                            }
                        },
                        onFail = {
                            feedbackMessage = "Daily limit reached!"
                            isLoading = false
                        }
                    )
                } else {
                    feedbackMessage = "Incorrect! Try again."
                }
            },
            enabled = !isLoading && userState.dailyCaptchaLimit > 0
        ) {
            Text(if (isLoading) "Loading Ad..." else "Submit Captcha")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(feedbackMessage, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}
INNER_EOF
