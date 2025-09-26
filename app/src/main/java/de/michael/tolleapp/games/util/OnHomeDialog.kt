package de.michael.tolleapp.games.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import de.michael.tolleapp.R

@Composable
fun OnHomeDialog(
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    onDiscard: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.save_game_title))
        },
        text = {
            Text(text = stringResource(R.string.save_game_desc))
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = saveEnabled,
            ) {
                Text(text = stringResource(R.string.save_game))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDiscard,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                )
            ) {
                Text(text = stringResource(R.string.discard_game))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        modifier = modifier
    )
}