package de.michael.tolleapp.games.util.table

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

fun String.toTableHeader(): (@Composable () -> Unit) {
    return { Text(
        text = this,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    ) }
}

fun String.toRowCell(): (@Composable () -> Unit) {
    return { Text(
        text = this,
    ) }
}