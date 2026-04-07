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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Primary
import com.khabarexpress.buyer.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val placeOrderState by viewModel.placeOrderState.collectAsState()
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }
    
    // Handle place order result
    LaunchedEffect(placeOrderState) {
        when (val state = placeOrderState) {
            is PlaceOrderState.Success -> {
                viewModel.resetPlaceOrderState()
                navController.navigate(Screen.OrderTracking.createRoute(state.order.id))
            }
            else -> { }
        }
    }
    
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
        when (val state = uiState) {
            is CheckoutUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is CheckoutUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is CheckoutUiState.Success -> {
                val summary = state.orderSummary
                val deliveryAddress = state.selectedAddress?.let {
                    "${it.houseNo}, ${it.roadNo}, ${it.area}"
                } ?: "No address selected"
                
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
                                                onClick = {
                                                    selectedPaymentMethod = method
                                                    viewModel.selectPaymentMethod(method)
                                                }
                                            )
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedPaymentMethod == method,
                                            onClick = {
                                                selectedPaymentMethod = method
                                                viewModel.selectPaymentMethod(method)
                                            }
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
                                PriceRow("Subtotal", summary.subtotal)
                                if (summary.discount > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PriceRow("Discount", -summary.discount)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                PriceRow("Delivery Fee", summary.deliveryFee)
                                Spacer(modifier = Modifier.height(8.dp))
                                PriceRow("Tax", summary.tax)
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
                                        text = "${Constants.CURRENCY_SYMBOL}${"%.2f".format(summary.total)}",
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
                            onClick = { viewModel.placeOrder() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            enabled = placeOrderState !is PlaceOrderState.Loading && state.selectedAddress != null
                        ) {
                            if (placeOrderState is PlaceOrderState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Place Order", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    
                    // Show error snackbar for place order errors
                    if (placeOrderState is PlaceOrderState.Error) {
                        LaunchedEffect(placeOrderState) {
                            // Error will be shown inline; reset after delay
                            kotlinx.coroutines.delay(3000)
                            viewModel.resetPlaceOrderState()
                        }
                    }
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
