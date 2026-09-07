package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingDealsScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Shopping Deals") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Top Affiliate Deals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            ActionCard(
                title = "Amazon Electronics",
                subtitle = "Up to 5% Cashback in Coins",
                icon = Icons.Default.ShoppingCart,
                onClick = { uriHandler.openUri("https://www.amazon.com/") }
            )
            ActionCard(
                title = "Flipkart Fashion",
                subtitle = "Up to 8% Cashback in Coins",
                icon = Icons.Default.ShoppingCart,
                onClick = { uriHandler.openUri("https://www.flipkart.com/") }
            )
            ActionCard(
                title = "Daraz Special",
                subtitle = "Up to 3% Cashback (Nepal)",
                icon = Icons.Default.ShoppingCart,
                onClick = { uriHandler.openUri("https://www.daraz.com.np/") }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Claim Missing Cashback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            var orderId by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }
            
            OutlinedTextField(
                value = orderId,
                onValueChange = { orderId = it },
                label = { Text("Enter Order ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { 
                    if (orderId.isNotEmpty()) {
                        isSubmitting = true
                        scope.launch {
                            delay(1500)
                            isSubmitting = false
                            snackbarHostState.showSnackbar("Order ID $orderId submitted for verification!")
                            orderId = ""
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    Text("Submitting...")
                } else {
                    Text("Submit for Verification")
                }
            }
        }
    }
}
