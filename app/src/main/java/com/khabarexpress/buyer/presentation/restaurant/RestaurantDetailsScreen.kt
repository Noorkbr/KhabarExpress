package com.khabarexpress.buyer.presentation.restaurant

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.khabarexpress.buyer.domain.model.*
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.*
import com.khabarexpress.buyer.util.Constants
import com.khabarexpress.buyer.util.SampleData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(
    restaurantId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with ViewModel and fetch restaurant by ID
    val restaurant = remember { 
        SampleData.getBangladeshRestaurants().find { it.id == restaurantId } 
            ?: SampleData.getBangladeshRestaurants().first()
    }
    
    var isFavorite by remember { mutableStateOf(false) }
    // Initial cart count - in production this would come from a ViewModel/Repository
    var cartItemCount by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isFavorite = !isFavorite
                        // In production: Call ViewModel to toggle favorite
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        BadgedBox(badge = { Badge { Text("$cartItemCount") } }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Restaurant Header
            item {
                Column {
                    // Cover Image
                    AsyncImage(
                        model = restaurant.coverImageUrl ?: restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Restaurant Info
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = restaurant.cuisine.joinToString(", "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Restaurant Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RestaurantStat(
                                icon = Icons.Filled.Star,
                                value = "${restaurant.rating}",
                                label = "${restaurant.totalReviews} reviews"
                            )
                            RestaurantStat(
                                icon = Icons.Filled.Schedule,
                                value = "${restaurant.deliveryTime} min",
                                label = "Delivery"
                            )
                            RestaurantStat(
                                icon = Icons.Filled.LocalShipping,
                                value = if (restaurant.deliveryFee == 0.0) "Free" else "${Constants.CURRENCY_SYMBOL}${restaurant.deliveryFee}",
                                label = "Delivery fee"
                            )
                        }
                    }
                    
                    HorizontalDivider()
                }
            }
            
            // Menu Categories
            restaurant.categories.forEach { category ->
                item {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                items(category.items) { menuItem ->
                    MenuItemCard(
                        item = menuItem,
                        onAddToCart = { 
                            cartItemCount++
                            // In production: Call ViewModel to add item to cart
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isVegetarian) {
                        Surface(
                            color = Success,
                            shape = RoundedCornerShape(2.dp),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${Constants.CURRENCY_SYMBOL}${"%.2f".format(item.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            // Item Image and Add Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (item.imageUrl != null) {
                    Box {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("ADD")
                        }
                    }
                } else {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("ADD")
                    }
                }
            }
        }
    }
}

// Sample restaurant details
