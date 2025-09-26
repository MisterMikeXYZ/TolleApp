package de.michael.tolleapp.games.util.table

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class TableStrokeOptions{
    START,
    END,
    ALL,
    NONE
}

data class TableStrokes(
    val vertical: TableStrokeOptions = TableStrokeOptions.ALL,
    val horizontal: TableStrokeOptions = TableStrokeOptions.ALL,
    val outer: Boolean = true,
    val color: Color = Color.Gray,
    val width: Dp = 1.dp
)
