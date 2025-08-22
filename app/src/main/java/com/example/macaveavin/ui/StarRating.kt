package com.example.macaveavin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StarRating(
    rating: Float,
    onRatingChange: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    max: Int = 5,
) {
    val current = rating.coerceIn(0f, max.toFloat())
    val isReadOnly = onRatingChange == null
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(max) { index ->
            val position = index + 1
            val filled = current >= position
            val symbol = if (filled) "★" else "☆"
            val color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            Text(
                text = symbol,
                color = color,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .let { base ->
                        if (isReadOnly) base else base.clickable {
                            // tap toggles: set to position, or clear to 0 if same value
                            val newVal = if (current == position.toFloat()) 0f else position.toFloat()
                            onRatingChange?.invoke(newVal)
                        }
                    }
            )
        }
    }
}
