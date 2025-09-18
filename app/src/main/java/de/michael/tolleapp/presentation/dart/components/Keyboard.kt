package de.michael.tolleapp.presentation.dart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Keyboard(
    onThrow: (value: String, multiplier: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (1..20).map { it.toString() } + "25"
    val chunkedRows = numbers.chunked(7) // 3 rows of 7
    var activeMultiplier by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First 3 rows (numbers 1..20 + 25)
        chunkedRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { number ->
                    if (number == "25") {
                        KeyButton(
                            text = number,
                            enabled = activeMultiplier == null || activeMultiplier == "Double",
                            onClick = {
                                onThrow(number, activeMultiplier ?: "")
                                activeMultiplier = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    else {
                        KeyButton(
                            text = number,
                            onClick = {
                                onThrow(number, activeMultiplier ?: "")
                                activeMultiplier = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton(
                text = "0",
                enabled = activeMultiplier == null,
                onClick = { onThrow("0", "") },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )

            KeyButton(
                text = "Double",
                enabled = activeMultiplier == null || activeMultiplier == "Double",
                onClick = {
                    if (activeMultiplier == null) activeMultiplier = "Double"
                    else if (activeMultiplier == "Double") activeMultiplier = null
                },
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(2f)
            )

            KeyButton(
                text = "Triple",
                enabled = activeMultiplier == null || activeMultiplier == "Triple",
                onClick = {
                    if (activeMultiplier == null) activeMultiplier = "Triple"
                    else if (activeMultiplier == "Triple") activeMultiplier = null
                },
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(2f)
            )

            KeyButton(
                text = "Back",
                enabled = activeMultiplier == null,
                onClick = onBack,
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(2f)
            )
        }
    }
}


@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val backgroundColor = if (enabled) MaterialTheme.colorScheme.surfaceVariant else Color.LightGray
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable (enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}