package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.*

/**
 * Price breakdown card component
 * 
 * Displays itemized price breakdown with subtotal, fees, discounts, and total
 * 
 * @param subtotal Subtotal amount
 * @param deliveryFee Delivery fee amount
 * @param tax Tax amount (optional)
 * @param discount Discount amount (optional)
 * @param modifier Modifier for customization
 * @param promoCode Applied promo code (optional)
 */
@Composable
fun PriceBreakdownCard(
    subtotal: Double,
    deliveryFee: Double,
    modifier: Modifier = Modifier,
    tax: Double? = null,
    discount: Double? = null,
    promoCode: String? = null
) {
    val total = subtotal + deliveryFee + (tax ?: 0.0) - (discount ?: 0.0)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bill Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            // Subtotal
            PriceRow(
                label = "Subtotal",
                amount = subtotal
            )
            
            // Delivery fee
            PriceRow(
                label = "Delivery Fee",
                amount = deliveryFee
            )
            
            // Tax
            if (tax != null && tax > 0) {
                PriceRow(
                    label = "Tax & Fees",
                    amount = tax
                )
            }
            
            // Discount
            if (discount != null && discount > 0) {
                PriceRow(
                    label = if (promoCode != null) "Discount ($promoCode)" else "Discount",
                    amount = -discount,
                    isDiscount = true
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "৳${String.format("%.0f", total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: Double,
    isDiscount: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isDiscount) {
                "-৳${String.format("%.0f", -amount)}"
            } else {
                "৳${String.format("%.0f", amount)}"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDiscount) Success else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Compact price summary for bottom bars
 */
@Composable
fun CompactPriceSummary(
    total: Double,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "৳${String.format("%.0f", total)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                text = "$itemCount items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceBreakdownCardPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PriceBreakdownCard(
                subtotal = 450.0,
                deliveryFee = 30.0,
                tax = 45.0,
                discount = 90.0,
                promoCode = "FIRST30"
            )
            
            Surface(
                color = Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactPriceSummary(
                    total = 435.0,
                    itemCount = 3,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
