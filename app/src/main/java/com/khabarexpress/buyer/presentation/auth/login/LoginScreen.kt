package com.khabarexpress.buyer.presentation.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Gold
import com.khabarexpress.buyer.ui.theme.Navy

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    // Validate phone on change
    LaunchedEffect(phoneNumber) {
        phoneError = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFCFAF6),
                        Color(0xFFF7F3EC)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Luxury brand header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseOutBack),
                    initialOffsetY = { -40 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Gold restaurant icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Gold.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = "KhabarExpress Logo",
                            modifier = Modifier.size(48.dp),
                            tint = Gold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "KHABAR EXPRESS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 4.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Welcome text
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your phone number to continue",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Phone number input (BD format)
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(
                    tween(600, delayMillis = 400),
                    initialOffsetY = { 30 }
                )
            ) {
                Column {
                    // Country code + phone input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            // Only allow digits, max 11 chars for BD format (01XXXXXXXXX)
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 11) {
                                phoneNumber = filtered
                            }
                        },
                        label = { Text("Phone Number") },
                        placeholder = { Text("01XXXXXXXXX") },
                        prefix = {
                            Text(
                                text = "+880  ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Navy
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Phone,
                                contentDescription = null,
                                tint = Gold
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = Gold,
                            focusedLabelColor = Gold
                        ),
                        isError = phoneError != null,
                        supportingText = phoneError?.let {
                            { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 500))
            ) {
                Button(
                    onClick = {
                        val fullNumber = if (phoneNumber.startsWith("0")) phoneNumber else "0$phoneNumber"
                        // Validate BD phone: must be 01X-XXXXXXXX
                        val isValid = fullNumber.matches(Regex("^01[3-9]\\d{8}$"))
                        if (!isValid) {
                            phoneError = "Please enter a valid Bangladeshi phone number"
                        } else {
                            // Navigate to home – seamless login, no verification needed
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Navy
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "CONTINUE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "or"
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 600))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "  or  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guest access
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 700))
            ) {
                TextButton(
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        "Continue as Guest",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Restaurant staff login link
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 800))
            ) {
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) }
                ) {
                    Text(
                        "Restaurant / Staff Login →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = Gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
