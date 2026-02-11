package com.foodshare.features.fridges.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.foodshare.features.fridges.domain.model.StockLevel

@Composable
fun FridgeMarker(
    stockLevel: StockLevel,
    modifier: Modifier = Modifier
) {
    val markerColor = when (stockLevel) {
        StockLevel.FULL -> Color(0xFF4CAF50)
        StockLevel.HALF -> Color(0xFFFFEB3B)
        StockLevel.LOW -> Color(0xFFFF9800)
        StockLevel.EMPTY -> Color(0xFFF44336)
        StockLevel.UNKNOWN -> Color.Gray
    }

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle with color based on stock level
        Surface(
            shape = CircleShape,
            color = markerColor.copy(alpha = 0.3f),
            modifier = Modifier
                .size(48.dp)
                .border(2.dp, markerColor, CircleShape)
        ) {}

        // Inner circle with fridge icon
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Kitchen,
                    contentDescription = "Fridge",
                    tint = markerColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Status badge
        if (stockLevel == StockLevel.EMPTY || stockLevel == StockLevel.LOW) {
            Surface(
                shape = CircleShape,
                color = Color.Red,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .border(2.dp, Color.White, CircleShape)
            ) {}
        }
    }
}
