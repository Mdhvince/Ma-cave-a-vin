package com.example.macaveavin.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier.semantics { contentDescription = "Note $current sur $max" },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(max) { index ->
            val position = index + 1
            val filled = current >= position
            val targetColor = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            val color by animateColorAsState(targetValue = targetColor, label = "starColor")
            val scale by animateFloatAsState(
                targetValue = if (filled) 1.15f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "starScale"
            )
            val symbol = if (filled) "★" else "☆"

            Text(
                text = symbol,
                color = color,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .scale(scale)
                    .let { base ->
                        if (isReadOnly) base else base
                            .semantics(mergeDescendants = false) {
                                role = Role.Button
                                contentDescription = "Donner $position étoiles"
                            }
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                // tap toggles: set to position, or clear to 0 if same value
                                val newVal = if (current == position.toFloat()) 0f else position.toFloat()
                                onRatingChange?.invoke(newVal)
                            }
                    }
            )
        }
    }
}
