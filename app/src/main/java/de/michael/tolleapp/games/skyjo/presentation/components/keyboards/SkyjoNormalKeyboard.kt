package de.michael.tolleapp.games.skyjo.presentation.components.keyboards


import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SkyjoNormalKeyboard(
    onSubmit: (total: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (1..9).map { it.toString() }
    val chunkedRows = numbers.chunked(3)

    var selectedValues by remember { mutableStateOf<List<String>>(emptyList()) }

    val inputString = selectedValues.joinToString("")
    val valuesAsInt = inputString.toIntOrNull()

    val invalidInput by remember(selectedValues, valuesAsInt) {
        derivedStateOf {
            selectedValues.isNotEmpty() &&
                    inputString != "-" && (valuesAsInt == null || valuesAsInt < -17 || valuesAsInt > 288)
        }
    }
    val enabler = !invalidInput
    val input = when {
        selectedValues.isEmpty() -> "Ergebnis eingeben"
        inputString == "-" -> "-"
        valuesAsInt == null || invalidInput -> "Unmöglicher Wert: $inputString"
        else -> inputString
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
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                tint = if (!selectedValues.isEmpty()) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(30.dp)
                    .clickable { selectedValues = selectedValues.dropLast(1) }
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.KeyboardHide,
                contentDescription = "Toggle keyboard",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onBack() }
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
                        enabled = enabler,
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
                text = "—",
                enabled = enabler,
                onClick = { selectedValues = selectedValues + "-" },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f)
            )

            SkyjoKeyButton(
                text = "0",
                enabled = enabler,
                onClick = { selectedValues = selectedValues + "0" },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2f)
            )

            SkyjoKeyButton(
                text = "Submit",
                onClick = {
                    onSubmit(inputString.toInt())
                    selectedValues = emptyList()
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f),
                enabled = enabler && !selectedValues.isEmpty(),
            )

        }
        Spacer(modifier = Modifier.padding(1.dp))
    }
}