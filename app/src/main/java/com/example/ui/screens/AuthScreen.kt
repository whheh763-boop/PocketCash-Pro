package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Country
import com.example.viewmodel.MainViewModel
import com.example.utils.SecurityManager

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onNavigateToHome: () -> Unit
) {
    var referralCode by remember { mutableStateOf("") }
    val userState by viewModel.userState.collectAsState()

    var isBlocked by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!SecurityManager.isDeviceSecure()) {
            isBlocked = true
        }
    }

    if (isBlocked) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Access Denied", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your device is flagged for potential abuse (Emulator/Root detected). PocketCash Pro is strictly for real devices.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("PocketCash Pro", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Earn rewards in ${userState.country.displayName}", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = referralCode,
            onValueChange = { referralCode = it },
            label = { Text("Referral Code (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Select Region:", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                selected = userState.country == Country.INDIA,
                onClick = { viewModel.updateCountry(Country.INDIA) },
                label = { Text("India (\u20B9)") }
            )
            FilterChip(
                selected = userState.country == Country.NEPAL,
                onClick = { viewModel.updateCountry(Country.NEPAL) },
                label = { Text("Nepal (NRs)") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onNavigateToHome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login with Google")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { onNavigateToHome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login with Phone OTP")
        }
    }
}
