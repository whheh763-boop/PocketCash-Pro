package com.example.ui.screens
import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val userState by viewModel.userState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var nameInput by remember { mutableStateOf(userState.displayName) }
    var paymentIdInput by remember { mutableStateOf(userState.paymentId) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cyber Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black // Let gradient show through
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))))
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Futuristic Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(20.dp, CircleShape, spotColor = Color(0xFF00FF87))
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E))
                        .border(3.dp, Brush.linearGradient(listOf(Color(0xFF00FF87), Color(0xFF60EFFF))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF00FF87)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Stats row
                val animatedCoins by animateIntAsState(
                    targetValue = userState.coinBalance,
                    animationSpec = tween(1500),
                    label = "profileCoinAnimation"
                )
                val animatedLifetime by animateIntAsState(
                    targetValue = userState.lifetimeEarnings,
                    animationSpec = tween(1500),
                    label = "profileLifetimeAnimation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatCard(title = "CURRENT COINS", value = animatedCoins.toString(), color = Color(0xFF00FF87))
                    ProfileStatCard(title = "LIFETIME EARNED", value = animatedLifetime.toString(), color = Color(0xFF60EFFF))
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Input fields with glassmorphism style
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF60EFFF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = paymentIdInput,
                    onValueChange = { paymentIdInput = it },
                    label = { Text("Payment ID (UPI/PayPal)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF60EFFF),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(nameInput, paymentIdInput)
                        showSavedMessage = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF00FF87)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87))
                ) {
                    Text("SAVE PROTOCOL", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }

                if (showSavedMessage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PROFILE SYNCHRONIZED", color = Color(0xFF00FF87), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60EFFF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60EFFF))
                ) {
                    Text("VIEW LEDGER", letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileStatCard(title: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Black)
        }
    }
}
