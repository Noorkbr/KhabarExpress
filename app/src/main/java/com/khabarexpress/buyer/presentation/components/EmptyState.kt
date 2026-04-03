package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.KhabarExpressTheme

/**
 * Generic empty state component
 * 
 * Displays an icon, title, message, and optional action button
 * for empty states like empty cart, no favorites, no orders, etc.
 * 
 * @param icon Icon to display
 * @param title Title text
 * @param message Description message
 * @param actionButtonText Optional action button text
 * @param onActionClick Callback when action button is clicked
 * @param modifier Modifier for customization
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = actionButtonText,
                onClick = onActionClick,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

/**
 * Pre-configured empty cart state
 */
@Composable
fun EmptyCartState(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Filled.ShoppingCart,
        title = "Your cart is empty",
        message = "Add items to get started with your order",
        actionButtonText = "Browse Restaurants",
        onActionClick = onBrowseClick,
        modifier = modifier
    )
}

/**
 * Pre-configured empty favorites state
 */
@Composable
fun EmptyFavoritesState(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Filled.FavoriteBorder,
        title = "No favorites yet",
        message = "Save your favorite restaurants for quick access",
        actionButtonText = "Explore Restaurants",
        onActionClick = onExploreClick,
        modifier = modifier
    )
}

/**
 * Pre-configured empty orders state
 */
@Composable
fun EmptyOrdersState(
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Filled.Receipt,
        title = "No orders yet",
        message = "Your order history will appear here",
        actionButtonText = "Order Now",
        onActionClick = onOrderClick,
        modifier = modifier
    )
}

/**
 * Pre-configured empty search results state
 */
@Composable
fun EmptySearchState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Filled.SearchOff,
        title = "No results found",
        message = "Try adjusting your search or filters",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            EmptyCartState(
                onBrowseClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
