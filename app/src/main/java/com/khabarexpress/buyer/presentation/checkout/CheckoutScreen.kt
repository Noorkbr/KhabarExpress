package com.khabarexpress.buyer.presentation.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Primary
import com.khabarexpress.buyer.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }
    val deliveryAddress = "House: 12, Road: 5, Gulshan 2, Dhaka-1212"
    val subtotal = 698.0
    val deliveryFee = 30.0
    val tax = 34.90
    val total = subtotal + deliveryFee + tax
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Delivery Address
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delivery Address",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = { navController.navigate(Screen.AddressSelection.route) }) {
                                Text("Change")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = deliveryAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Method
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PaymentMethod.values().forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedPaymentMethod == method,
                                        onClick = { selectedPaymentMethod = method }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = when (method) {
                                        PaymentMethod.BKASH -> Icons.Filled.Wallet
                                        PaymentMethod.NAGAD -> Icons.Filled.Wallet
                                        PaymentMethod.ROCKET -> Icons.Filled.Wallet
                                        PaymentMethod.UPAY -> Icons.Filled.Wallet
                                        PaymentMethod.SSL_COMMERZ -> Icons.Filled.CreditCard
                                        PaymentMethod.CASH_ON_DELIVERY -> Icons.Filled.Money
                                        PaymentMethod.CREDIT_CARD -> Icons.Filled.CreditCard
                                        PaymentMethod.DEBIT_CARD -> Icons.Filled.Payment
                                    },
                                    contentDescription = null,
                                    tint = if (selectedPaymentMethod == method) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (method) {
                                        PaymentMethod.BKASH -> "bKash"
                                        PaymentMethod.NAGAD -> "Nagad"
                                        PaymentMethod.ROCKET -> "Rocket"
                                        PaymentMethod.UPAY -> "Upay"
                                        PaymentMethod.SSL_COMMERZ -> "Card Payment (SSL Commerz)"
                                        PaymentMethod.CASH_ON_DELIVERY -> "Cash on Delivery"
                                        PaymentMethod.CREDIT_CARD -> "Credit Card"
                                        PaymentMethod.DEBIT_CARD -> "Debit Card"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Order Summary
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Order Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PriceRow("Subtotal", subtotal)
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceRow("Delivery Fee", deliveryFee)
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceRow("Tax", tax)
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${Constants.CURRENCY_SYMBOL}${"%.2f".format(total)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }
            
            // Place Order Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Button(
                    onClick = {
                        // TODO: Place order
                        navController.navigate(Screen.OrderTracking.createRoute("order123"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Place Order", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${Constants.CURRENCY_SYMBOL}${"%.2f".format(amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
