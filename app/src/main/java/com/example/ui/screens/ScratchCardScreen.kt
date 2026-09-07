package com.example.ui.screens
import androidx.compose.ui.graphics.nativeCanvas

import android.app.Activity
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdsManager
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ScratchCardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isScratched by remember { mutableStateOf(false) }
    var scratchPath by remember { mutableStateOf(Path()) }
    var earnedCoins by remember { mutableStateOf(0) }
    var showRewardDialog by remember { mutableStateOf(false) }

    // Scratching logic
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var scratchedArea by remember { mutableFloatStateOf(0f) }

    // Shimmer effect animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // Random reward when screen loads
    LaunchedEffect(Unit) {
        earnedCoins = listOf(15, 25, 50, 10, 5, 100).random()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Scratch Card", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scratch the card to reveal your prize!",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Find up to 1000 coins inside!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Premium Card View
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), RoundedCornerShape(24.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Reward text underneath
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "You Won!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFA500),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$earnedCoins",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2ECC71)
                    )
                    Text(
                        "Coins",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scratch layer
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isScratched,
                    exit = fadeOut(tween(1000)),
                    modifier = Modifier.matchParentSize()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 0.99f } // Needed for BlendMode.Clear to work in Canvas
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        currentPath = Path().apply {
                                            moveTo(event.x, event.y)
                                        }
                                        true
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        currentPath?.lineTo(event.x, event.y)
                                        scratchPath.addPath(currentPath!!)
                                        currentPath = Path().apply { moveTo(event.x, event.y) }
                                        
                                        // Approximate scratched area check
                                        scratchedArea += 1f
                                        if (scratchedArea > 150f && !isScratched) {
                                            isScratched = true
                                            
                                            // Show Ad when scratched
                                            activity?.let {
                                                AdsManager.showRewardedAd(
                                                    activity = it,
                                                    onRewardEarned = {
                                                        viewModel.addCoins(earnedCoins)
                                                        showRewardDialog = true
                                                    },
                                                    onAdDismissed = {
                                                        if (!showRewardDialog) {
                                                            viewModel.addCoins(earnedCoins)
                                                            showRewardDialog = true
                                                        }
                                                    }
                                                )
                                            } ?: run {
                                                viewModel.addCoins(earnedCoins)
                                                showRewardDialog = true
                                            }
                                        }
                                        true
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        currentPath = null
                                        true
                                    }
                                    else -> false
                                }
                            }
                    ) {
                        val cardWidth = size.width
                        val cardHeight = size.height
                        
                        // Draw the scratch cover gradient
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                                start = Offset(0f, 0f),
                                end = Offset(cardWidth, cardHeight)
                            ),
                            size = size
                        )
                        
                        // Shimmer effect over the cover
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                                start = Offset(shimmerOffset, shimmerOffset),
                                end = Offset(shimmerOffset + 300f, shimmerOffset + 300f)
                            ),
                            size = size
                        )
                        
                        // Decorative text on cover
                        drawContext.canvas.nativeCanvas.let { canvas ->
                            canvas.drawText(
                                "SCRATCH ME",
                                cardWidth / 2,
                                cardHeight / 2,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 60f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                            )
                        }

                        // Draw the scratch path with BlendMode.Clear to reveal underneath
                        drawPath(
                            path = scratchPath,
                            color = Color.Transparent,
                            style = Stroke(width = 100f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
            }
        }
    }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRewardDialog = false
                onBack()
            },
            title = { Text("Card Scratched!", fontWeight = FontWeight.Bold, color = PremiumPrimary) },
            text = { 
                Text(
                    "You found $earnedCoins coins! They have been securely added to your balance.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showRewardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPrimary)
                ) {
                    Text("Awesome, Thanks!")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
