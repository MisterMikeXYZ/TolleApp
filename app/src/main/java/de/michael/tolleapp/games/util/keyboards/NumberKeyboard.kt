package de.michael.tolleapp.games.util.keyboards

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.keyboards.components.KeyboardButton

private const val TAG = "NumberKeyboard"

@Composable
fun NumberKeyboard(
    onSubmit: (total: Int) -> Unit,
    hideKeyboard: () -> Unit,
    initialValue: Int? = null,
    minusAllowed: Boolean = false,
    withDoubleSubmit: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    var currentInput by remember { mutableStateOf(initialValue?.toString() ?: "") }

    val validInput by remember(currentInput) {
        derivedStateOf {
            currentInput.toIntOrNull()?.let { minusAllowed || it >= 0 } ?: false
        }
    }
    val inputPrefix = when {
        currentInput.isEmpty() -> "Ergebnis eingeben"
        !validInput -> "Unmöglicher Wert: "
        else -> ""
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val scrollState = rememberScrollState()
            LaunchedEffect(currentInput) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Text(
                text = inputPrefix + currentInput,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp, end = 16.dp)
                    .horizontalScroll(scrollState)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { currentInput = currentInput.dropLast(1) }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardHide,
                contentDescription = "Toggle keyboard",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { hideKeyboard() }
            )
        }

        (1..9).chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { number ->
                    KeyboardButton(
                        text = number.toString(),
                        enabled = currentInput.isEmpty() || validInput,
                        onClick = { currentInput += number.toString() },
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
            KeyboardButton(
                text = if (!withDoubleSubmit) "-" else "Submit x2",
                enabled = if (!withDoubleSubmit) minusAllowed && validInput
                    else validInput,
                onClick = {
                    if (!withDoubleSubmit) currentInput += "-"
                    else currentInput.toIntOrNull()?.let{
                        onSubmit(it * 2)
                        currentInput = ""
                    } ?: Log.e(TAG, "Could not parse input '$currentInput' to Int")
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f)
            )
            KeyboardButton(
                text = "0",
                enabled = currentInput.isEmpty() || validInput,
                onClick = { currentInput += "0" },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2f)
            )
            KeyboardButton(
                text = "Submit",
                onClick = {
                    currentInput.toIntOrNull()?.let{
                        onSubmit(it)
                        currentInput = ""
                    } ?: Log.e(TAG, "Could not parse input '$currentInput' to Int")
                },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(2.5f),
                enabled = validInput,
            )
        }
        Spacer(modifier = Modifier.padding(1.dp))
    }
}