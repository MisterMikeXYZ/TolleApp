package de.michael.tolleapp.games.util.keyboards

import android.R.attr.enabled
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.keyboards.components.ExtraButton
import de.michael.tolleapp.games.util.keyboards.components.ExtraButtonType
import de.michael.tolleapp.games.util.keyboards.components.KeyboardButton
import de.michael.tolleapp.games.util.keyboards.components.createExtraButton
import kotlin.math.ceil

@Composable
fun CustomSingleCardKeyboard(
    onSubmit: (total: Int) -> Unit,
    onBack: () -> Unit,
    lowestNumber: Int,
    highestNumber: Int,
    numberOfRows: Int,
    maxNumbers: Int = 100,
    extraButtonTypes: List<ExtraButtonType> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val extraButtons = extraButtonTypes.map { createExtraButton(it) }
    val allButtons = (lowestNumber..highestNumber).map { it.toString() } + extraButtons.map { it.label } +  "Submit"

    val totalItems = allButtons.size
    val itemsPerRow = ceil(totalItems / numberOfRows.toDouble()).toInt()
    val chunkedRows = allButtons.chunked(itemsPerRow)

    var selectedValues by remember { mutableStateOf<List<Int>>(emptyList()) }
    val total = selectedValues.sum()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val scrollState = rememberScrollState()
        LaunchedEffect(selectedValues) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Text(
                selectedValues.takeUnless { it.isEmpty() }?.joinToString(", ")
                    ?: "Wähle Kartenwerte aus",
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 2.dp, end = 70.dp)
                    .horizontalScroll(scrollState)
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
            ) {
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
        }

        val buttonHeight = 60.dp

        chunkedRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { value ->
                    val isSubmit = value == "Submit"
                    val isNumber = value.toIntOrNull() != null
                    val isSpecial = extraButtons.find { it.label == value }

                    KeyboardButton(
                        text = value,
                        enabled = when {
                            isSubmit -> selectedValues.isNotEmpty()
                            isNumber -> selectedValues.size < maxNumbers // TODO make configurable
                            isSpecial != null -> isSpecial.enabled
                            else -> false
                        },
                        onClick = {
                            when {
                                isSubmit -> {
                                    onSubmit(total)
                                    selectedValues = emptyList()
                                }

                                isNumber -> {
                                    if (selectedValues.size < maxNumbers) {
                                        selectedValues = selectedValues + value.toInt()
                                    }
                                }

                                isSpecial != null -> {
                                    selectedValues = isSpecial.onClick(selectedValues)
                                }
                            }
                        },
                        modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                                //.aspectRatio(1.5f)
                        )
                }
            }
        }
        Spacer(modifier = Modifier.padding(3.dp))
    }
}