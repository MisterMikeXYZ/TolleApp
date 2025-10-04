package de.michael.tolleapp.games.util.endScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import de.michael.tolleapp.games.util.table.toTableHeader
import de.michael.tolleapp.games.util.table.toTableRowCell
import de.michael.tolleapp.games.util.table.toTableTotalCell

@Composable
fun EndScreen(
    titleValue: String,
    sortedPlayerNames: List<String>,
    sortedScoreValues: List<List<String>>,
    sortedTotalValues: List<String>? = null,
    navigateToMainMenu: () -> Unit,
    undoLastRound: () -> Unit = { },
    playAgain: () -> Unit = { },
    showWinner: Boolean = false,
) {
    Scaffold (
        topBar = {
            CustomTopBar(
                title = "$titleValue - Endstand",
                navigationIcon = {
                    IconButton(navigateToMainMenu) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Zum Hauptmenü"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { undoLastRound() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .padding(pad)
        ) {
            if (showWinner) {
                val winner = sortedPlayerNames.first()
                Text(
                    text = "\uD83C\uDFC6 $winner \uD83C\uDFC6",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(12.dp))
            }
            Table(
                headers = (listOf("") + sortedPlayerNames.mapIndexed { i, name -> "${i + 1}. $name" }).map { it.toTableHeader() },
                rows = sortedScoreValues.mapIndexed { index, rowValues ->
                    (listOf((index + 1).toString()) + rowValues).map { it.toTableRowCell() }
                },
                totalRow = sortedTotalValues?.let { sortedTotalValues ->
                    (listOf("∑") + sortedTotalValues).map { it.toTableTotalCell() }
                },
                frozenStartColumns = 1,
                weights = listOf(1f) + List(sortedPlayerNames.size) { 2.5f },
                minCellWidth = 32.dp,
                cellPadding = 4.dp,
                tableStrokes = TableStrokes(
                    vertical = TableStrokeOptions.ALL,
                    horizontal = TableStrokeOptions.START_END,
                    outer = false,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    width = 2.dp
                ),
                headerBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            )
            Spacer(Modifier.height(12.dp))
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(navigateToMainMenu) {
                    Text("Zum Hauptmenü")
                }
                Spacer(Modifier.size(16.dp))
                OutlinedButton(playAgain) {
                    Text("Nochmal spielen")
                }
            }
        }
    }
}