package de.michael.tolleapp.games.wizard.presentation.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.R
import de.michael.tolleapp.games.skyjo.presentation.components.BetterOutlinedTextField

@Composable
fun WizardPlayerItem(
    playerName: String,
    bidValue: String,
    wonTricksValue: String,
    onBidChange: (String) -> Unit,
    onWonTricksChange: (String) -> Unit,
    isDealer: Boolean,
    inputEnabled: Pair<Boolean, Boolean>,
    highlight: Boolean,
    keyboardAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = playerName,
            style = if (isDealer) MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                else LocalTextStyle.current,
            color = if (isDealer) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(0.4f),
        )
        BetterOutlinedTextField(
            value = bidValue,
            onValueChange = onBidChange,
            label = stringResource(R.string.bid),
            enabled = inputEnabled.first,
            highlight = inputEnabled.first && highlight,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (keyboardAction == null) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                keyboardAction?.let {
                    it.invoke()
                    focusManager.clearFocus()
                }
                    ?: focusManager.moveFocus(FocusDirection.Next)
            },
            modifier = Modifier.weight(0.3f)
        )
        BetterOutlinedTextField(
            value = wonTricksValue,
            onValueChange = onWonTricksChange,
            label = stringResource(R.string.won),
            enabled = inputEnabled.second,
            highlight = inputEnabled.second && highlight,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = if (keyboardAction == null) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions {
                keyboardAction?.let {
                    it.invoke()
                    focusManager.clearFocus()
                }
                    ?: focusManager.moveFocus(FocusDirection.Next)
            },
            modifier = Modifier.weight(0.3f)
        )
    }
}