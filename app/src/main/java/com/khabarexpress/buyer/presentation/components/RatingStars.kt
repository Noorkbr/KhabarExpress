package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.KhabarExpressTheme
import com.khabarexpress.buyer.ui.theme.Rating
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Rating stars component
 * 
 * Displays rating as stars (filled/half/empty) with numeric value
 * 
 * @param rating Rating value (0.0 to 5.0)
 * @param modifier Modifier for customization
 * @param showNumeric Whether to show numeric rating value
 * @param reviewCount Optional review count to display
 * @param starSize Size of each star
 * @param starColor Color for filled stars
 */
@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    showNumeric: Boolean = true,
    reviewCount: Int? = null,
    starSize: Dp = 16.dp,
    starColor: Color = Rating
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Draw 5 stars
        repeat(5) { index ->
            val starValue = index + 1
            val icon = when {
                rating >= starValue -> Icons.Filled.Star
                rating >= starValue - 0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.StarOutline
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (rating >= starValue - 0.5) starColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(starSize)
            )
        }
        
        if (showNumeric || reviewCount != null) {
            Spacer(modifier = Modifier.width(4.dp))
            
            if (showNumeric) {
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (reviewCount != null) {
                Text(
                    text = " ($reviewCount)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Large rating display for detail screens
 */
@Composable
fun LargeRating(
    rating: Double,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Rating
        )
        
        RatingStars(
            rating = rating,
            showNumeric = false,
            starSize = 24.dp
        )
        
        Text(
            text = "$reviewCount reviews",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact rating for list items
 */
@Composable
fun CompactRating(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Rating,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingStarsPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RatingStars(
                rating = 4.5,
                reviewCount = 230
            )
            
            RatingStars(
                rating = 3.7,
                showNumeric = true
            )
            
            RatingStars(
                rating = 5.0,
                reviewCount = 1523,
                starSize = 20.dp
            )
            
            LargeRating(
                rating = 4.8,
                reviewCount = 542
            )
            
            CompactRating(rating = 4.2)
        }
    }
}
