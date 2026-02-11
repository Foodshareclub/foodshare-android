package com.foodshare.features.help.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.foodshare.ui.design.components.layout.GlassExpander
import com.foodshare.ui.design.tokens.Spacing

data class FAQItem(val question: String, val answer: String)

@Composable
fun FAQSection(
    title: String,
    items: List<FAQItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = Spacing.sm)
        )
        items.forEach { item ->
            var expanded by remember { mutableStateOf(false) }
            GlassExpander(
                title = item.question,
                isExpanded = expanded,
                onToggle = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs)
            ) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(Spacing.md)
                )
            }
        }
    }
}
