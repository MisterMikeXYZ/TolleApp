package de.michael.tolleapp.games.util

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun DividedScreen(
    topPart: @Composable () -> Unit,
    bottomPart: @Composable () -> Unit,
    startTopFraction: Float = 0.5f,
    minFraction: Float = 0.1f,
    maxFraction: Float = 0.9f,
    modifier: Modifier = Modifier
) {
    var topHeightFraction by remember { mutableFloatStateOf(startTopFraction) }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .weight(topHeightFraction)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)),
                )
        ) {
            topPart()
        }

        // --- DIVIDER ---
        Box(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val parentHeight = 1f // normalized since we use weight
                        val change = delta / 1700f // tune sensitivity
                        topHeightFraction = (topHeightFraction + change)
                            .coerceIn(minFraction, maxFraction)
                    }
                )
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            HorizontalDivider(
                thickness = 4.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .width(64.dp)
                    .align(Alignment.Center)
            )
        }

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .weight(1f - topHeightFraction)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
                )
        ) {
            bottomPart()
        }
    }
}