package com.papercode.composecourse.stuff.table

import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Table(
    headers: List<@Composable () -> Unit>,
    rows: List<List<@Composable () -> Unit>>,
    weights: List<Float> = List(headers.size) { 1f },
    modifier: Modifier = Modifier,
    tableStrokes: TableStrokes = TableStrokes(),
    headerBackgroundColor: Color = Color.LightGray,
    cellPadding: Dp = 8.dp,
) {

    val borderStroke = BorderStroke(tableStrokes.width, tableStrokes.color)
    val rowScrollState = rememberLazyListState()

    Column (
        modifier = modifier.then(
            if (tableStrokes.outer) Modifier.border(borderStroke) else Modifier
        )
    ) {
        Row(
            modifier = Modifier.background(headerBackgroundColor)
        ) {
            headers.forEachIndexed { index, header ->
                Box(
                    modifier = Modifier
                        .weight(weights.getOrElse(index) { 1f })
                        .then(
                            if (
                                (tableStrokes.vertical == TableStrokeOptions.ALL ||
                                        (tableStrokes.vertical == TableStrokeOptions.START && index == 0) ||
                                        (tableStrokes.vertical == TableStrokeOptions.END && index == headers.size - 2)
                                        ) && index < headers.size - 1
                            ) {
                                Modifier.drawBehind {
                                    val strokeWidth = tableStrokes.width.toPx()
                                    drawLine(
                                        color = tableStrokes.color,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            } else Modifier
                        )
                        .padding(cellPadding),
                    contentAlignment = Alignment.Center
                ) {
                    header()
                }
            }
        }

        // Horizontal border after header
        if (tableStrokes.horizontal == TableStrokeOptions.ALL || tableStrokes.horizontal == TableStrokeOptions.START) {
            Divider(
                color = tableStrokes.color,
                thickness = tableStrokes.width
            )
        }

        LazyColumn(
            state = rowScrollState,
            contentPadding = PaddingValues(0.dp)
        ) {
            items(rows.size) { rowIndex ->
                val row = rows[rowIndex]

                Row {
                    row.forEachIndexed { colIndex, cell ->
                        Box(
                            modifier = Modifier
                                .weight(weights.getOrElse(colIndex) { 1f })
                                .then(
                                    if (
                                        (tableStrokes.vertical == TableStrokeOptions.ALL ||
                                                (tableStrokes.vertical == TableStrokeOptions.START && colIndex == 0) ||
                                                (tableStrokes.vertical == TableStrokeOptions.END && colIndex == headers.size - 2)
                                                ) && colIndex < headers.size - 1
                                    ) {
                                        Modifier.drawBehind {
                                            val strokeWidth = tableStrokes.width.toPx()
                                            drawLine(
                                                color = tableStrokes.color,
                                                start = Offset(size.width, 0f),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                    } else Modifier
                                )
                                .padding(cellPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            cell()
                        }
                    }
                }

                // Horizontal border between rows
                if ((
                    tableStrokes.horizontal == TableStrokeOptions.ALL ||
                            (tableStrokes.horizontal == TableStrokeOptions.END && rowIndex == rows.size - 2)
                    ) && rowIndex < rows.size - 1
                ) {
                    Divider(
                        color = tableStrokes.color,
                        thickness = tableStrokes.width
                    )
                }
            }
        }
    }
}

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
                Person("Tom", 27, "Arlington"),Person("Fiona", 27, "Philadelphia"),
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

    val headers = listOf<@Composable () -> Unit>(
        { Text("Name", fontWeight = FontWeight.Bold) },
        { Text("Age", fontWeight = FontWeight.Bold) },
        { Text("City", fontWeight = FontWeight.Bold) }
    )

    val cells = people.map { person ->
        listOf<@Composable () -> Unit>(
            { Text(person.name) },
            { Text(person.age.toString()) },
            { Text(person.city) }
        )
    }


    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(16.dp)
            .fillMaxHeight()

    ) {
        Table(
            headers = headers,
            rows = cells,
            tableStrokes = TableStrokes(
                vertical = TableStrokeOptions.START,
                horizontal = TableStrokeOptions.START,
                outer = false,
                color = Color.Black,
                width = 3.dp
            ),
            weights = listOf(2f, 1f, 2f),
            headerBackgroundColor = Color(0xFFE0E0FF),
            modifier = Modifier
                .fillMaxHeight(.4f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F0FF))
        )
    }
}

