package de.michael.tolleapp.games.skyjo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SkyjoNeleKeyboard(
    onSubmit: (total: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = (-2..12).map { it.toString() } + "Submit"
    val chunkedRows = numbers.chunked(4)

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
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Undo,
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
                row.forEach { value ->
                    SkyjoKeyButton(
                        text = value,
                        enabled = true,
                        onClick = {
                            if(value != "Submit") {
                                value.toIntOrNull()?.let {
                                    if (selectedValues.size < 12) selectedValues = selectedValues + it
                                }
                            } else {
                                onSubmit(total.toString())
                                selectedValues = emptyList()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.5f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(3.dp))
    }
}