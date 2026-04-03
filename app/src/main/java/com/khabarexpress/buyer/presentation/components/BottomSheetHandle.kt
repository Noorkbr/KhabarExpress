package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.KhabarExpressTheme

/**
 * Bottom sheet drag handle component
 * 
 * Standard visual indicator for draggable bottom sheets
 * 
 * @param modifier Modifier for customization
 */
@Composable
fun BottomSheetHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetHandlePreview() {
    KhabarExpressTheme {
        Column {
            BottomSheetHandle()
        }
    }
}
