package de.michael.tolleapp.games.util.table

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun String.toTableHeader(
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Bold,
): (@Composable () -> Unit) {
    return { Text(
        text = this,
        maxLines = maxLines,
        overflow = overflow,
        style = style,
        fontWeight = fontWeight,
    ) }
}

fun String.toTableRowCell(
    maxLines: Int = 1,
): (@Composable () -> Unit) {
    return { Text(
        text = this,
        maxLines = maxLines
    ) }
}

fun String.toTableTotalCell(
    maxLines: Int = 1,
    fontWeight: FontWeight = FontWeight.SemiBold,
): (@Composable () -> Unit) {
    return { Text(
        text = this,
        maxLines = maxLines,
        fontWeight = fontWeight
    ) }
}