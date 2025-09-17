package de.michael.tolleapp.presentation.dart.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.presentation.dart.DartState
import de.michael.tolleapp.presentation.dart.DartViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerScoreDisplays(
    playerId: String,
    viewModel: DartViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    val playerName = state.playerNames[playerId] ?: "Unknown"
    val totalRemaining = state.totalPoints[playerId] ?: 0

    val rounds = state.perPlayerRounds[playerId] ?: emptyList()
    val currentThrows = rounds.lastOrNull().orEmpty()
    val previousThrows = if (rounds.size > 1) rounds[rounds.size - 2] else emptyList()

    Row (
        modifier = modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        //Name
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        Column (
            modifier = Modifier.weight(4f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                currentThrows.forEach { throwValue ->
                    Text(
                        text = throwValue.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        //Totalpoints
        Column (
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$totalRemaining",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }


        Spacer(modifier = Modifier.height(4.dp))

        // Current throws row


        // Previous throws row (greyed out)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            previousThrows.forEach { throwValue ->
                Text(
                    text = throwValue.toString(),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
