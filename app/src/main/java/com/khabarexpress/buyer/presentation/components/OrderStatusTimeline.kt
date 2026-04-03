package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.*

/**
 * Order status timeline component
 * 
 * Displays vertical timeline showing order progression through different states
 * 
 * @param currentStep Current step index (0-based)
 * @param steps List of step labels
 * @param timestamps Optional timestamps for each step
 * @param modifier Modifier for customization
 */
@Composable
fun OrderStatusTimeline(
    currentStep: Int,
    steps: List<String> = defaultOrderSteps,
    timestamps: List<String?> = List(steps.size) { null },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        steps.forEachIndexed { index, step ->
            TimelineStep(
                stepNumber = index + 1,
                stepLabel = step,
                timestamp = timestamps.getOrNull(index),
                isCompleted = index < currentStep,
                isCurrent = index == currentStep,
                isLast = index == steps.lastIndex
            )
        }
    }
}

@Composable
private fun TimelineStep(
    stepNumber: Int,
    stepLabel: String,
    timestamp: String?,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = when {
                    isCompleted -> Success
                    isCurrent -> Primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                border = if (isCurrent && !isCompleted) {
                    androidx.compose.foundation.BorderStroke(2.dp, Primary)
                } else null
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Connector line
            if (!isLast) {
                VerticalDivider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp),
                    color = if (isCompleted) Success else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Step details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 40.dp else 0.dp)
        ) {
            Text(
                text = stepLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isCompleted || isCurrent -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
            
            if (timestamp != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isCurrent && !isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "In progress...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private val defaultOrderSteps = listOf(
    "Order Placed",
    "Order Confirmed",
    "Preparing",
    "Ready for Pickup",
    "Picked Up",
    "On the Way",
    "Delivered"
)

@Preview(showBackground = true)
@Composable
private fun OrderStatusTimelinePreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Status",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OrderStatusTimeline(
                currentStep = 3,
                timestamps = listOf(
                    "10:30 AM",
                    "10:32 AM",
                    "10:35 AM",
                    "10:50 AM",
                    null,
                    null,
                    null
                )
            )
        }
    }
}
