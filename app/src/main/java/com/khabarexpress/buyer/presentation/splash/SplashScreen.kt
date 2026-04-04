package com.khabarexpress.buyer.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.presentation.auth.login.LoginViewModel
import com.khabarexpress.buyer.ui.theme.Gold
import com.khabarexpress.buyer.ui.theme.Navy
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var showLogo by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showPartner by remember { mutableStateOf(false) }
    var showAmbassador by remember { mutableStateOf(false) }

    // Animate the gold accent line
    val infiniteTransition = rememberInfiniteTransition(label = "splash_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        showLogo = true
        delay(600)
        showTagline = true
        delay(500)
        showPartner = true
        delay(500)
        showAmbassador = true
        delay(1400)

        val isAuthenticated = false // viewModel.isAuthenticated()
        if (isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Navy,
                        Color(0xFF0F1D36)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animated brand logo text
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(animationSpec = tween(800)) +
                        slideInVertically(
                            animationSpec = tween(800, easing = EaseOutBack),
                            initialOffsetY = { -80 }
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KHABAR",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 48.sp,
                            letterSpacing = 8.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                    Text(
                        text = "EXPRESS",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            letterSpacing = 12.sp
                        ),
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Glowing gold accent line
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .alpha(glowAlpha)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Gold,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "Your Premium Food Experience",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        letterSpacing = 2.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Digital partner showcase
            AnimatedVisibility(
                visible = showPartner,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DIGITAL PARTNER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 3.sp
                        ),
                        color = Gold.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KhabarExpress Technologies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand ambassador
            AnimatedVisibility(
                visible = showAmbassador,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BRAND AMBASSADOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 3.sp
                        ),
                        color = Gold.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nijhum Sarker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gold.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
