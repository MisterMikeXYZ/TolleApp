package de.michael.tolleapp.games.skyjo.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
fun SkyjoKeyboard(
    onSubmit: (total: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (-2..12).map { it.toString() }
    val chunkedRows = numbers.chunked(5)

    var selectedValues by remember { mutableStateOf<List<Int>>(emptyList()) }
    val total = selectedValues.sum()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (selectedValues.isEmpty()) Text("Wähle Karten aus") else {
                Text(
                    selectedValues.joinToString(", "),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        chunkedRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { number ->
                    SkyjoKeyButton(
                        text = number,
                        enabled = true,
                        onClick = {
                            number.toIntOrNull()?.let { if (selectedValues.size < 12) selectedValues = selectedValues + it }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkyjoKeyButton(
                text = "Submit",
                onClick = {
                    onSubmit(total.toString())
                    selectedValues = emptyList()
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f),
                enabled = true,
            )

            SkyjoKeyButton(
                text = "Remove",
                onClick = { selectedValues = selectedValues.dropLast(1) },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f),
                enabled = true
            )
        }
    }
}

@Composable
fun SkyjoKeyButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondary,
            textAlign = TextAlign.Center
        )
    }
}