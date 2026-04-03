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
import com.khabarexpress.buyer.ui.theme.Error

/**
 * Error screen component
 * 
 * Full screen error display with icon, message, and retry button
 * 
 * @param message Error message to display
 * @param modifier Modifier for customization
 * @param icon Error icon (default: Error icon)
 * @param title Error title (default: "Something went wrong")
 * @param onRetry Callback when retry button is clicked
 * @param retryButtonText Text for retry button
 */
@Composable
fun ErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.ErrorOutline,
    title: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    retryButtonText: String = "Try Again"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Error.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = retryButtonText,
                onClick = onRetry,
                modifier = Modifier.widthIn(max = 200.dp)
            )
        }
    }
}

/**
 * Network error screen variant
 */
@Composable
fun NetworkErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        message = "Please check your internet connection and try again",
        title = "No Internet Connection",
        icon = Icons.Filled.WifiOff,
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Not found error screen variant
 */
@Composable
fun NotFoundErrorScreen(
    message: String = "The page you're looking for doesn't exist",
    onGoBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        message = message,
        title = "Not Found",
        icon = Icons.Filled.SearchOff,
        onRetry = onGoBack,
        retryButtonText = "Go Back",
        modifier = modifier
    )
}

/**
 * Server error screen variant
 */
@Composable
fun ServerErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        message = "Our servers are having issues. Please try again later",
        title = "Server Error",
        icon = Icons.Filled.CloudOff,
        onRetry = onRetry,
        modifier = modifier
    )
}

/**
 * Inline error message for forms and smaller areas
 */
@Composable
fun InlineError(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = "Error",
            tint = Error,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Error
        )
    }
}

/**
 * Error alert dialog
 */
@Composable
fun ErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    confirmText: String = "OK",
    dismissText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Error
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm ?: onDismiss) {
                Text(confirmText)
            }
        },
        dismissButton = if (onConfirm != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        } else null
    )
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NetworkErrorScreen(
                onRetry = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineErrorPreview() {
    KhabarExpressTheme {
        InlineError(
            message = "Please enter a valid email address"
        )
    }
}
