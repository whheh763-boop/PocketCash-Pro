package com.example.ui.screens
import androidx.compose.ui.graphics.nativeCanvas

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdsManager
import com.example.ui.theme.PremiumPrimary
import com.example.ui.theme.PremiumSecondary
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinWheelScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val prizes = listOf(10, 50, 0, 100, 20, 5)
    val colors = listOf(
        Color(0xFF6C5CE7), Color(0xFFFFD700), Color(0xFFE74C3C), 
        Color(0xFF2ECC71), Color(0xFF9B59B6), Color(0xFF3498DB)
    )

    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinsLeft by remember { mutableStateOf(10) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var earnedCoins by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spin & Win", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black // Let background gradient show through
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF141E30), Color(0xFF243B55))))
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Daily Spins Left: $spinsLeft/10",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Wheel UI (Premium Design)
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .shadow(24.dp, CircleShape, spotColor = Color(0xFFFFD700))
                        .border(8.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF39C12))), CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation.value)
                    ) {
                        val sweepAngle = 360f / prizes.size
                        val radius = size.width / 2

                        for (i in prizes.indices) {
                            // Slices
                            drawArc(
                                brush = Brush.radialGradient(listOf(colors[i], colors[i].copy(alpha = 0.7f))),
                                startAngle = i * sweepAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                style = Fill
                            )
                            // Slice Borders
                            drawArc(
                                color = Color.White.copy(alpha = 0.5f),
                                startAngle = i * sweepAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                style = Stroke(width = 4.dp.toPx())
                            )
                            
                            // Text Drawing
                            val angleInRadians = Math.toRadians((i * sweepAngle + sweepAngle / 2).toDouble())
                            val textRadius = radius * 0.65f
                            val x = (center.x + textRadius * cos(angleInRadians)).toFloat()
                            val y = (center.y + textRadius * sin(angleInRadians)).toFloat()

                            drawContext.canvas.nativeCanvas.let { canvas ->
                                canvas.save()
                                canvas.rotate((i * sweepAngle + sweepAngle / 2 + 90f), x, y)
                                canvas.drawText(
                                    "${prizes[i]}",
                                    x,
                                    y,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 60f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                        setShadowLayer(4f, 0f, 4f, android.graphics.Color.BLACK)
                                    }
                                )
                                canvas.restore()
                            }
                        }
                    }

                    // Center pin/button
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color.White, Color.LightGray)))
                            .border(4.dp, Color(0xFFF39C12), CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(40.dp))
                    }
                }
                
                // Outer Pointer Pin
                Box(modifier = Modifier.offset(y = (-330).dp)) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width / 2 - 20.dp.toPx(), 0f)
                            lineTo(size.width / 2 + 20.dp.toPx(), 0f)
                            lineTo(size.width / 2, 40.dp.toPx())
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(listOf(Color(0xFFE74C3C), Color(0xFFC0392B)))
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (!isSpinning && spinsLeft > 0) {
                            isSpinning = true
                            spinsLeft -= 1

                            scope.launch {
                                val winningIndex = (0 until prizes.size).random()
                                val targetRotation = rotation.value + 360f * 5 + (360f - (winningIndex * (360f / prizes.size)))

                                rotation.animateTo(
                                    targetValue = targetRotation,
                                    animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
                                )

                                earnedCoins = prizes[winningIndex]
                                isSpinning = false

                                // Show Rewarded Ad when spin finishes
                                activity?.let {
                                    AdsManager.showRewardedAd(
                                        activity = it,
                                        onRewardEarned = {
                                            viewModel.addCoins(earnedCoins)
                                            showRewardDialog = true
                                        },
                                        onAdDismissed = {
                                            if (!showRewardDialog && earnedCoins > 0) {
                                                viewModel.addCoins(earnedCoins)
                                                showRewardDialog = true
                                            } else if (earnedCoins == 0) {
                                                showRewardDialog = true
                                            }
                                        }
                                    )
                                } ?: run {
                                    viewModel.addCoins(earnedCoins)
                                    showRewardDialog = true
                                }
                            }
                        }
                    },
                    enabled = !isSpinning && spinsLeft > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF39C12),
                        disabledContainerColor = Color.Gray
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("SPIN TO WIN", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text(if (earnedCoins > 0) "Congratulations! 🎉" else "Oops! 😢", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (earnedCoins > 0) "You won $earnedCoins coins!" else "Better luck next time! Try again.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRewardDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary)
                ) {
                    Text("Awesome")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
