package de.michael.tolleapp.games.util.table

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* --------------------------------- Table -------------------------------- */

@Composable
fun Table(
    headers: List<@Composable () -> Unit>,
    rows: List<List<@Composable () -> Unit>>,
    totalRow: List<@Composable () -> Unit>? = null,
    weights: List<Float> = List(headers.size) { 1f },
    tableStrokes: TableStrokes = TableStrokes(),
    headerBackgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    cellPadding: Dp = 8.dp,
    /** Minimum width per 1f weight unit. Triggers horizontal scrolling when needed. */
    minCellWidth: Dp = 24.dp,
    /** Keep the total row visible at the bottom (vertically pinned). */
    pinTotalRow: Boolean = true,
    /** Number of leading columns that stay fixed (not horizontally scrollable). */
    frozenStartColumns: Int = 0,
    modifier: Modifier = Modifier,
) {
    require(headers.isNotEmpty()) { "headers must not be empty" }
    require(weights.size >= headers.size) { "weights must have at least headers.size elements" }

    val cols = headers.size
    val frozen = frozenStartColumns.coerceIn(0, cols)
    val scrollColsStart = frozen
    val scrollColsCount = cols - frozen

    val borderStroke = BorderStroke(tableStrokes.width, tableStrokes.color)

    // One vertical list state (single LazyColumn for body -> synced by design)
    val verticalListState = rememberLazyListState()
    // One horizontal state for scrollable (right) side (shared by header/body/footer)
    val sharedHorizontalScroll = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.then(if (tableStrokes.outer) Modifier.border(borderStroke) else Modifier)
    ) {
        val totalWeight = weights.take(cols).sum().coerceAtLeast(0.0001f)
        val frozenWeight = weights.take(frozen).sum()
        val scrollWeight = weights.drop(frozen).take(scrollColsCount).sum()

        val minFrozenWidth = minCellWidth * frozenWeight
        val minScrollWidth = minCellWidth * scrollWeight
        val minTotalWidth = minCellWidth * totalWeight

        val availableWidth = this.maxWidth

        // Viewport width for the frozen area
        val frozenViewportWidth =
            if (frozen == 0) 0.dp
            else if (availableWidth >= minTotalWidth) {
                availableWidth * (frozenWeight / totalWeight)
            } else {
                minOf(availableWidth, minFrozenWidth)
            }

        // Viewport width for the scrollable area (visible width without horizontal scroll)
        val scrollViewportWidth = (availableWidth - frozenViewportWidth).coerceAtLeast(0.dp)

        // Content width for scrollable area (can exceed viewport to enable horizontal scroll)
        val scrollContentWidth =
            if (availableWidth >= minTotalWidth) scrollViewportWidth
            else maxOf(scrollViewportWidth, minScrollWidth)

        // ---- Use SubcomposeLayout to measure header & footer first, then body with the remaining height ----
        SubcomposeLayout { constraints ->
            val maxW = constraints.maxWidth
            val maxH = constraints.maxHeight

            // 1) HEADER (includes its divider)
            val headerPlaceables = subcompose("header") {
                Column(modifier = Modifier.background(headerBackgroundColor)) {
                    Row {
                        // Left (frozen) header
                        if (frozen > 0) {
                            Row(modifier = Modifier.width(frozenViewportWidth)) {
                                RenderCells(
                                    start = 0,
                                    endExclusive = frozen,
                                    totalCols = cols,
                                    headersOrRow = headers,
                                    weights = weights,
                                    tableStrokes = tableStrokes,
                                    cellPadding = cellPadding
                                )
                            }
                        }
                        // Right (scrollable) header
                        CompositionLocalProvider(LocalOverscrollFactory provides null) {
                            Column(
                                modifier = Modifier
                                    .width(scrollViewportWidth)
                                    .horizontalScroll(sharedHorizontalScroll)
                            ) {
                                Row(modifier = Modifier.width(scrollContentWidth)) {
                                    RenderCells(
                                        start = scrollColsStart,
                                        endExclusive = cols,
                                        totalCols = cols,
                                        headersOrRow = headers,
                                        weights = weights,
                                        tableStrokes = tableStrokes,
                                        cellPadding = cellPadding,
                                    )
                                }
                            }
                        }
                    }
                    if (tableStrokes.horizontal == TableStrokeOptions.ALL
                        || tableStrokes.horizontal == TableStrokeOptions.START_END
                        || tableStrokes.horizontal == TableStrokeOptions.START
                    ) {
                        Row {
                            if (frozen > 0) {
                                Box(Modifier.width(frozenViewportWidth)) {
                                    HorizontalDivider(thickness = tableStrokes.width, color = tableStrokes.color)
                                }
                            }
                            Box(Modifier.width(scrollViewportWidth)) {
                                HorizontalDivider(thickness = tableStrokes.width, color = tableStrokes.color)
                            }
                        }
                    }
                }
            }.map { it.measure(constraints.copy(minHeight = 0)) }
            val headerH = headerPlaceables.maxOfOrNull { it.height } ?: 0

            // 2) FOOTER (pinned) measured before body so body gets the leftover space
            val footerPlaceables =
                if (pinTotalRow && totalRow != null) {
                    subcompose("footer") {
                        Column {
                            FooterDecorations(tableStrokes)
                            Row {
                                if (frozen > 0) {
                                    Row(modifier = Modifier.width(frozenViewportWidth)) {
                                        RenderCells(
                                            start = 0,
                                            endExclusive = frozen,
                                            totalCols = cols,
                                            headersOrRow = totalRow,
                                            weights = weights,
                                            tableStrokes = tableStrokes,
                                            cellPadding = cellPadding
                                        )
                                    }
                                }
                                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                                    Column(
                                        modifier = Modifier
                                            .width(scrollViewportWidth)
                                            .horizontalScroll(sharedHorizontalScroll)
                                    ) {
                                        Row(modifier = Modifier.width(scrollContentWidth)) {
                                            RenderCells(
                                                start = scrollColsStart,
                                                endExclusive = cols,
                                                totalCols = cols,
                                                headersOrRow = totalRow,
                                                weights = weights,
                                                tableStrokes = tableStrokes,
                                                cellPadding = cellPadding
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }.map { it.measure(constraints.copy(minHeight = 0)) }
                } else emptyList()
            val footerH = footerPlaceables.maxOfOrNull { it.height } ?: 0

            // 3) BODY gets the remaining height; wraps when short, scrolls when tall
            val bodyMaxH = (maxH - headerH - footerH).coerceAtLeast(0)
            val bodyPlaceables = subcompose("body") {
                // SINGLE LazyColumn for both frozen + scrollable parts per row
                LazyColumn(
                    state = verticalListState,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    itemsIndexed(rows) { rowIndex, row ->
                        Row {
                            if (frozen > 0) {
                                Row(modifier = Modifier.width(frozenViewportWidth)) {
                                    RenderCells(
                                        start = 0,
                                        endExclusive = frozen,
                                        totalCols = cols,
                                        headersOrRow = row,
                                        weights = weights,
                                        tableStrokes = tableStrokes,
                                        cellPadding = cellPadding,
                                    )
                                }
                            }
                            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                                Column(
                                    modifier = Modifier
                                        .width(scrollViewportWidth)
                                        .horizontalScroll(sharedHorizontalScroll)
                                ) {
                                    Row(modifier = Modifier.width(scrollContentWidth)) {
                                        RenderCells(
                                            start = scrollColsStart,
                                            endExclusive = cols,
                                            totalCols = cols,
                                            headersOrRow = row,
                                            weights = weights,
                                            tableStrokes = tableStrokes,
                                            cellPadding = cellPadding,
                                        )
                                    }
                                }
                            }
                        }
                        if ((tableStrokes.horizontal == TableStrokeOptions.ALL ||
                                    (tableStrokes.horizontal == TableStrokeOptions.END && rowIndex == rows.size - 2))
                            && rowIndex < rows.size - 1
                        ) {
                            Row {
                                if (frozen > 0) {
                                    Box(Modifier.width(frozenViewportWidth)) {
                                        HorizontalDivider(thickness = tableStrokes.width, color = tableStrokes.color)
                                    }
                                }
                                Box(Modifier.width(scrollViewportWidth)) {
                                    HorizontalDivider(thickness = tableStrokes.width, color = tableStrokes.color)
                                }
                            }
                        }
                    }

                    // If totalRow is NOT pinned, include it as part of the list (unchanged)
                    if (!pinTotalRow && totalRow != null) {
                        item {
                            FooterDecorations(tableStrokes)
                            Row {
                                if (frozen > 0) {
                                    Row(modifier = Modifier.width(frozenViewportWidth)) {
                                        RenderCells(
                                            start = 0,
                                            endExclusive = frozen,
                                            totalCols = cols,
                                            headersOrRow = totalRow,
                                            weights = weights,
                                            tableStrokes = tableStrokes,
                                            cellPadding = cellPadding,
                                        )
                                    }
                                }
                                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                                    Column(
                                        modifier = Modifier
                                            .width(scrollViewportWidth)
                                            .horizontalScroll(sharedHorizontalScroll)
                                    ) {
                                        Row(modifier = Modifier.width(scrollContentWidth)) {
                                            RenderCells(
                                                start = scrollColsStart,
                                                endExclusive = cols,
                                                totalCols = cols,
                                                headersOrRow = totalRow,
                                                weights = weights,
                                                tableStrokes = tableStrokes,
                                                cellPadding = cellPadding
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }.map {
                it.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = bodyMaxH
                    )
                )
            }
            val bodyH = bodyPlaceables.maxOfOrNull { it.height } ?: 0

            val totalH = (headerH + bodyH + footerH).coerceAtLeast(0)

            layout(maxW, totalH) {
                var y = 0
                headerPlaceables.forEach { it.placeRelative(0, y); y += it.height }
                bodyPlaceables.forEach { it.placeRelative(0, y); y += it.height }
                footerPlaceables.forEach { it.placeRelative(0, y) }
            }
        }
    }
}

/* ----------------------------- Helper UIs ----------------------------- */

@Composable
private fun FooterDecorations(tableStrokes: TableStrokes) {
    if (tableStrokes.horizontal == TableStrokeOptions.ALL
        || tableStrokes.horizontal == TableStrokeOptions.START_END
        || tableStrokes.horizontal == TableStrokeOptions.END
    ) {
        HorizontalDivider(thickness = tableStrokes.width * 0.3f, color = tableStrokes.color)
        Spacer(Modifier.height(tableStrokes.width * 0.4f))
        HorizontalDivider(thickness = tableStrokes.width * 0.3f, color = tableStrokes.color)
    }
}

@Composable
private fun RenderCells(
    start: Int,
    endExclusive: Int,
    totalCols: Int,
    headersOrRow: List<@Composable () -> Unit>,
    weights: List<Float>,
    tableStrokes: TableStrokes,
    cellPadding: Dp,
) {
    Row {
        for (index in start until endExclusive) {
            val cell = headersOrRow[index]
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(weights.getOrElse(index) { 1f })
                    .then(
                        if (shouldDrawVerticalStroke(index, totalCols, tableStrokes))
                            Modifier.drawBehind {
                                val strokeWidth = tableStrokes.width.toPx()
                                drawLine(
                                    color = tableStrokes.color,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                        else Modifier
                    )
                    .padding(cellPadding)
            ) {
                cell()
            }
        }
    }
}

private fun shouldDrawVerticalStroke(
    globalIndex: Int,
    totalCols: Int,
    strokes: TableStrokes
): Boolean {
    val isNotLastColumn = globalIndex < totalCols - 1
    return isNotLastColumn && (
            strokes.vertical == TableStrokeOptions.ALL ||
                    (strokes.vertical == TableStrokeOptions.START && globalIndex == 0) ||
                    (strokes.vertical == TableStrokeOptions.END && globalIndex == totalCols - 2)
            )
}

/* -------------------------------- Preview ------------------------------- */

@Preview
@Composable
fun TableExample() {
    data class Person(val name: String, val age: Int, val city: String)

    val people = listOf(
        Person("Alice", 30, "New York"),
        Person("Bob", 25, "Los Angeles"),
        Person("Charlie", 35, "Chicago"),
        Person("Diana", 28, "Houston"),
        Person("Ethan", 32, "Phoenix"),
        Person("Fiona", 27, "Philadelphia"),
        Person("George", 29, "San Antonio"),
        Person("Hannah", 31, "San Diego"),
        Person("Ian", 26, "Dallas"),
        Person("Julia", 33, "San Jose"),
        Person("Kevin", 24, "Austin"),
        Person("Laura", 28, "Jacksonville"),
        Person("Mike", 34, "Fort Worth"),
        Person("Nina", 30, "Columbus"),
        Person("Oscar", 32, "Charlotte"),
        Person("Paula", 27, "San Francisco"),
        Person("Quentin", 29, "Indianapolis"),
        Person("Rachel", 31, "Seattle"),
        Person("Sam", 25, "Denver"),
        Person("Tina", 33, "Washington"),
        Person("Uma", 26, "Boston"),
        Person("Victor", 34, "El Paso"),
        Person("Wendy", 28, "Nashville"),
        Person("Xander", 30, "Detroit"),
        Person("Yara", 32, "Oklahoma City"),
        Person("Zane", 27, "Las Vegas"),
        Person("Ava", 29, "Louisville"),
        Person("Ben", 31, "Baltimore"),
        Person("Cara", 25, "Milwaukee"),
        Person("Derek", 33, "Albuquerque"),
        Person("Ella", 26, "Tucson"),
        Person("Felix", 34, "Fresno"),
        Person("Gina", 28, "Mesa"),
        Person("Harry", 30, "Sacramento"),
        Person("Isla", 32, "Atlanta"),
        Person("Jack", 27, "Kansas City"),
        Person("Kara", 29, "Colorado Springs"),
        Person("Liam", 31, "Miami"),
        Person("Mona", 25, "Raleigh"),
        Person("Noah", 33, "Omaha"),
        Person("Olive", 26, "Long Beach"),
        Person("Peter", 34, "Virginia Beach"),
        Person("Queen", 28, "Oakland"),
        Person("Rex", 30, "Minneapolis"),
        Person("Sara", 32, "Tulsa"),
        Person("Tom", 27, "Arlington"),
    )

    val rounds = List(6) { List(people.size) { (1..100).random() } }

    val headers = listOf<@Composable () -> Unit>(
        { Text("", fontWeight = FontWeight.Bold) },
    ) + people.map { { Text(it.name, fontWeight = FontWeight.Bold) } }

    val cells = rounds.mapIndexed { index, round ->
        (listOf(index + 1) + round).map { score ->
            @Composable { Text(score.toString()) }
        }
    }

    val totalRow = listOf<@Composable () -> Unit>(
        { Text("∑", fontWeight = FontWeight.Bold) },
    ) + people.mapIndexed { i, _ ->
        @Composable { Text(rounds.sumOf { it[i] }.toString(), fontWeight = FontWeight.Bold) }
    }

    Box(
        modifier = Modifier
            .padding(top = 64.dp)
            .background(Color.White)
            .width(340.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF0F0FF))
    ) {
        Table(
            headers = headers,
            rows = cells,
            totalRow = totalRow,
            tableStrokes = TableStrokes(
                vertical = TableStrokeOptions.START,
                horizontal = TableStrokeOptions.START_END,
                outer = false,
                color = Color.Black,
                width = 3.dp
            ),
            weights = listOf(1f) + List(people.size) { 3f },
            headerBackgroundColor = Color(0xFFE0E0FF),
            minCellWidth = 32.dp,
            pinTotalRow = true,
            frozenStartColumns = 1 // first column fixed
        )
    }
}
