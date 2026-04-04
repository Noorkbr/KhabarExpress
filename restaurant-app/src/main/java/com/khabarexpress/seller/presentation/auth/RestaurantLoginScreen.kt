package com.khabarexpress.seller.presentation.auth

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.khabarexpress.seller.navigation.RestaurantScreen

// Luxury brand colors matching the buyer app palette
private val Gold = Color(0xFFD4A03C)
private val Navy = Color(0xFF1B2A4A)
private val GoldLight = Color(0xFFF5E6C8)

@Composable
fun RestaurantLoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    LaunchedEffect(phoneNumber) { phoneError = null }
    LaunchedEffect(password) { passwordError = null }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, Color(0xFF0F1D36))
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

            // Restaurant brand header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseOutBack),
                    initialOffsetY = { -40 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = "Restaurant",
                        modifier = Modifier.size(56.dp),
                        tint = Gold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "KHABAR EXPRESS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = 4.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = Gold
                    )
                    Text(
                        text = "RESTAURANT PORTAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 4.sp
                        ),
                        color = Color.White.copy(alpha = 0.6f)
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
                        text = "Staff Login",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter your mobile number and password",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Phone number field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(
                    tween(600, delayMillis = 300),
                    initialOffsetY = { 30 }
                )
            ) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 11) {
                            phoneNumber = filtered
                        }
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("01XXXXXXXXX") },
                    prefix = {
                        Text(
                            text = "+880  ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Gold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Gold)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Gold,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                        focusedPrefixColor = Gold,
                        unfocusedPrefixColor = Gold.copy(alpha = 0.7f)
                    ),
                    isError = phoneError != null,
                    supportingText = phoneError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(
                    tween(600, delayMillis = 400),
                    initialOffsetY = { 30 }
                )
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Gold,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    isError = passwordError != null,
                    supportingText = passwordError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 500))
            ) {
                Button(
                    onClick = {
                        var hasError = false
                        val fullNumber = if (phoneNumber.startsWith("0")) phoneNumber else "0$phoneNumber"
                        val isValidPhone = fullNumber.matches(Regex("^01[3-9]\\d{8}$"))
                        if (!isValidPhone) {
                            phoneError = "Please enter a valid Bangladeshi phone number"
                            hasError = true
                        }
                        if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters"
                            hasError = true
                        }
                        if (!hasError) {
                            navController.navigate(RestaurantScreen.Dashboard.route) {
                                popUpTo(RestaurantScreen.Login.route) { inclusive = true }
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
                        "LOGIN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
