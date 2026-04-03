package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.KhabarExpressTheme
import com.khabarexpress.buyer.ui.theme.Primary

/**
 * Quantity selector component
 * 
 * Displays minus button, quantity, and plus button with min/max constraints
 * 
 * @param quantity Current quantity
 * @param onQuantityChange Callback when quantity changes
 * @param modifier Modifier for customization
 * @param minQuantity Minimum allowed quantity
 * @param maxQuantity Maximum allowed quantity
 * @param enabled Whether the selector is enabled
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int = 99,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus button
            IconButton(
                onClick = { 
                    if (quantity > minQuantity) {
                        onQuantityChange(quantity - 1)
                    }
                },
                enabled = enabled && quantity > minQuantity,
                modifier = Modifier.size(32.dp)
            ) {
                Surface(
                    color = if (enabled && quantity > minQuantity) Primary else Primary.copy(alpha = 0.3f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease quantity",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                }
            }
            
            // Quantity display
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.widthIn(min = 24.dp)
            )
            
            // Plus button
            IconButton(
                onClick = { 
                    if (quantity < maxQuantity) {
                        onQuantityChange(quantity + 1)
                    }
                },
                enabled = enabled && quantity < maxQuantity,
                modifier = Modifier.size(32.dp)
            ) {
                Surface(
                    color = if (enabled && quantity < maxQuantity) Primary else Primary.copy(alpha = 0.3f),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase quantity",
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact quantity selector for smaller spaces
 */
@Composable
fun CompactQuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = 1,
    maxQuantity: Int = 99
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Primary,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                    if (quantity > minQuantity) {
                        onQuantityChange(quantity - 1)
                    }
                },
                enabled = quantity > minQuantity,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.widthIn(min = 20.dp)
            )
            
            IconButton(
                onClick = { 
                    if (quantity < maxQuantity) {
                        onQuantityChange(quantity + 1)
                    }
                },
                enabled = quantity < maxQuantity,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantitySelectorPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var quantity1 by remember { mutableStateOf(1) }
            QuantitySelector(
                quantity = quantity1,
                onQuantityChange = { quantity1 = it }
            )
            
            var quantity2 by remember { mutableStateOf(5) }
            QuantitySelector(
                quantity = quantity2,
                onQuantityChange = { quantity2 = it },
                enabled = false
            )
            
            var quantity3 by remember { mutableStateOf(2) }
            CompactQuantitySelector(
                quantity = quantity3,
                onQuantityChange = { quantity3 = it }
            )
        }
    }
}
