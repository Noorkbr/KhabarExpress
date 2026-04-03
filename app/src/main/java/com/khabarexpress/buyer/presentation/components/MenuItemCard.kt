package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.khabarexpress.buyer.domain.model.MenuItem
import com.khabarexpress.buyer.ui.theme.*

/**
 * Reusable menu item card component
 * 
 * Displays menu item with image, name, description, price, dietary icons,
 * and add to cart functionality with quantity selector.
 * 
 * @param menuItem Menu item data
 * @param quantity Current quantity in cart (0 if not added)
 * @param onQuantityChange Callback when quantity changes
 * @param onCustomize Callback when customization is requested
 * @param modifier Modifier for customization
 */
@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    quantity: Int = 0,
    onQuantityChange: (Int) -> Unit,
    onCustomize: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Item image
            if (menuItem.imageUrl != null) {
                AsyncImage(
                    model = menuItem.imageUrl,
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Dietary icons
                if (menuItem.isVegetarian || menuItem.isVegan || menuItem.isGlutenFree) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        if (menuItem.isVegan) {
                            DietaryBadge(text = "Vegan", color = Success)
                        } else if (menuItem.isVegetarian) {
                            DietaryBadge(text = "Veg", color = Success)
                        }
                        if (menuItem.isGlutenFree) {
                            DietaryBadge(text = "GF", color = Info)
                        }
                    }
                }
                
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (menuItem.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "৳${String.format("%.0f", menuItem.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        
                        // Customization indicator
                        if (menuItem.customizations.isNotEmpty()) {
                            Text(
                                text = "Customizable",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Add to cart or quantity selector
                    if (!menuItem.isAvailable) {
                        Text(
                            text = "Not Available",
                            style = MaterialTheme.typography.labelMedium,
                            color = Error
                        )
                    } else if (quantity == 0) {
                        OutlinedButton(
                            onClick = { 
                                if (menuItem.customizations.isNotEmpty()) {
                                    onCustomize()
                                } else {
                                    onQuantityChange(1)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Primary
                            ),
                            border = BorderStroke(2.dp, Primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ADD",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        QuantitySelector(
                            quantity = quantity,
                            onQuantityChange = onQuantityChange,
                            minQuantity = 0,
                            maxQuantity = 10
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DietaryBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuItemCardPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuItemCard(
                menuItem = MenuItem(
                    id = "1",
                    name = "Chicken Tikka Masala",
                    description = "Tender chicken pieces in creamy tomato sauce",
                    price = 250.0,
                    imageUrl = "https://example.com/image.jpg",
                    isVegetarian = false,
                    isAvailable = true
                ),
                quantity = 0,
                onQuantityChange = {}
            )
            
            MenuItemCard(
                menuItem = MenuItem(
                    id = "2",
                    name = "Paneer Butter Masala",
                    description = "Cottage cheese in rich butter gravy",
                    price = 220.0,
                    imageUrl = "https://example.com/image.jpg",
                    isVegetarian = true,
                    isGlutenFree = true,
                    isAvailable = true,
                    customizations = listOf()
                ),
                quantity = 2,
                onQuantityChange = {}
            )
        }
    }
}
