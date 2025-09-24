package de.michael.tolleapp.games.skyjo.presentation.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun SkyjoNeleKeyboard(
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
            val scrollState = rememberScrollState()
            LaunchedEffect(selectedValues) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Text(
                selectedValues.takeUnless { it.isEmpty() }?.joinToString(", ")
                    ?: "Wähle Kartenwerte aus",
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 2.dp, end = 16.dp)
                    .horizontalScroll(scrollState)
            )
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
                            number.toIntOrNull()?.let {
                                if (selectedValues.size < 12) selectedValues = selectedValues + it
                            }
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