package com.example.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Country
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val userState by viewModel.userState.collectAsState()
    val minWithdrawalCoins = 1000
    val scope = rememberCoroutineScope()
    var withdrawalStatus by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("") }
    
    val methods = if (userState.country == Country.INDIA) {
        listOf("UPI (PhonePe, GPay)", "Paytm Wallet", "Mobile Recharge (₹10 for 1000 Coins)")
    } else {
        listOf("eSewa Wallet", "Khalti", "IME Pay", "Mobile Recharge")
    }

    if (selectedMethod.isEmpty() && methods.isNotEmpty()) {
        selectedMethod = methods.first()
    }

    val convertedValue = (userState.coinBalance / 1000.0) * userState.country.exchangeRatePer1000
    
    val animatedCoins by animateIntAsState(
        targetValue = userState.coinBalance,
        animationSpec = tween(1000),
        label = "walletCoinAnimation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet & Withdraw") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available Balance", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$animatedCoins Coins",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Equivalent to ${userState.country.currencySymbol}${String.format("%.2f", convertedValue)}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Select Payout Method (${userState.country.displayName})", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            methods.forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                    Text(method, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            var accountId by remember { mutableStateOf("") }
            OutlinedTextField(
                value = accountId,
                onValueChange = { accountId = it },
                label = { Text("Enter ID / Mobile Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    scope.launch {
                        withdrawalStatus = "Processing request..."
                        delay(1500)
                        withdrawalStatus = "Withdrawal request submitted for Admin approval."
                    }
                },
                enabled = userState.coinBalance >= minWithdrawalCoins && accountId.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Withdraw (Min $minWithdrawalCoins Coins)")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Transaction History")
            }
            
            if (withdrawalStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(withdrawalStatus, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
