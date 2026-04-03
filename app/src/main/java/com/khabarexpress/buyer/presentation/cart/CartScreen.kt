package com.khabarexpress.buyer.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.khabarexpress.buyer.domain.model.*
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Primary
import com.khabarexpress.buyer.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with ViewModel in production
    var cartItems by remember { mutableStateOf(getSampleCartItems()) }
    val deliveryFee = 20.0
    val subtotal = cartItems.sumOf { it.totalPrice }
    val total = subtotal + deliveryFee
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cart (${cartItems.size} items)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            // Empty cart state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your cart is empty",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add items to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.navigate(Screen.Home.route) }) {
                    Text("Browse Restaurants")
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            onIncreaseQuantity = {
                                cartItems = cartItems.map {
                                    if (it.id == item.id) it.copy(quantity = it.quantity + 1)
                                    else it
                                }
                            },
                            onDecreaseQuantity = {
                                if (item.quantity > 1) {
                                    cartItems = cartItems.map {
                                        if (it.id == item.id) it.copy(quantity = it.quantity - 1)
                                        else it
                                    }
                                } else {
                                    cartItems = cartItems.filter { it.id != item.id }
                                }
                            },
                            onRemoveItem = {
                                cartItems = cartItems.filter { it.id != item.id }
                            }
                        )
                    }
                }
                
                // Price Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        PriceRow("Subtotal", subtotal)
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceRow("Delivery Fee", deliveryFee)
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate(Screen.Checkout.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Proceed to Checkout", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncreaseQuantity: () -> Unit = {},
    onDecreaseQuantity: () -> Unit = {},
    onRemoveItem: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item Image
            AsyncImage(
                model = item.menuItem.imageUrl ?: "https://via.placeholder.com/80",
                contentDescription = item.menuItem.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.menuItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${Constants.CURRENCY_SYMBOL}${"%.2f".format(item.menuItem.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Primary
                )
                if (item.specialInstructions != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: ${item.specialInstructions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Quantity Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDecreaseQuantity) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = onIncreaseQuantity) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
                IconButton(onClick = onRemoveItem) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PriceRow(label: String, amount: Double) {
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

// Sample data
fun getSampleCartItems() = listOf(
    CartItem(
        id = "1",
        menuItem = MenuItem(
            id = "1",
            name = "Margherita Pizza",
            description = "Classic pizza with tomato sauce and mozzarella",
            price = 299.0,
            imageUrl = "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=400",
            isVegetarian = true
        ),
        quantity = 2
    ),
    CartItem(
        id = "2",
        menuItem = MenuItem(
            id = "2",
            name = "Garlic Bread",
            description = "Crispy bread with garlic butter",
            price = 99.0,
            imageUrl = "https://images.unsplash.com/photo-1573140401552-388e8e2940c9?w=400",
            isVegetarian = true
        ),
        quantity = 1,
        specialInstructions = "Extra cheese please"
    )
)
