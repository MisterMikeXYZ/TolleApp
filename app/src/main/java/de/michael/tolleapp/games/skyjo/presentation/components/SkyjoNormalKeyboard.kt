package de.michael.tolleapp.games.skyjo.presentation.components


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SkyjoNormalKeyboard(
    onSubmit: (total: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (1..9).map { it.toString() }
    val chunkedRows = numbers.chunked(3)

    var selectedValues by remember { mutableStateOf<List<String>>(emptyList()) }
    val valuesAsInt = selectedValues.joinToString("").toIntOrNull()
    val invalidInput by remember(selectedValues, valuesAsInt) {
        derivedStateOf {
            selectedValues.isNotEmpty() && (valuesAsInt == null || valuesAsInt < -17 || valuesAsInt > 144)
        }
    }
    val input = selectedValues
        .takeUnless { it.isEmpty() }
        ?.joinToString("")
        ?.takeUnless { valuesAsInt == null ||invalidInput }
        ?: when {
            selectedValues.isEmpty() -> "Wähle Kartenwerte aus"
            else -> "Unmöglicher Wert: ${selectedValues.joinToString("")}"
        }

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
                text = input,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 2.dp, end = 16.dp)
                    .horizontalScroll(scrollState)
            )
            IconButton(
                onClick = { selectedValues = selectedValues.dropLast(1) },
                modifier = Modifier.padding(all = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = null,
                    tint = if (!selectedValues.isEmpty()) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
                        onClick = { selectedValues = selectedValues + number },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(2f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkyjoKeyButton(
                text = "-",
                enabled = true,
                onClick = { selectedValues = selectedValues + "-" },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f)
            )

            SkyjoKeyButton(
                text = "0",
                enabled = true,
                onClick = { selectedValues = selectedValues + "0" },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2f)
            )

            SkyjoKeyButton(
                text = "Submit",
                onClick = {
                    onSubmit(selectedValues.joinToString(""))
                    selectedValues = emptyList()
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f),
                enabled = !invalidInput && !selectedValues.isEmpty(),
            )

        }






    }
}