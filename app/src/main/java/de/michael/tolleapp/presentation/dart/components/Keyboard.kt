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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Keyboard(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (1..20).map { it.toString() } + "25"
    val chunkedRows = numbers.chunked(7) // 3 rows of 7

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
                    KeyButton(
                        text = number,
                        onClick = { onKeyClick(number) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeyButton(
                text = "0",
                onClick = { onKeyClick("0") },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            listOf("Double", "Triple", "Back").forEach { label ->
                KeyButton(
                    text = label,
                    onClick = { onKeyClick(label) },
                    modifier = Modifier
                        .weight(2f)
                        .aspectRatio(2f)
                )
            }
        }
    }
}


@Composable
fun KeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}