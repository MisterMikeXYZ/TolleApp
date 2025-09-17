package de.michael.tolleapp.presentation.dart.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.presentation.dart.DartState
import de.michael.tolleapp.presentation.dart.DartViewModel
import de.michael.tolleapp.ui.theme.TolleAppTheme
import org.koin.compose.viewmodel.koinViewModel

@Preview
@Composable
private fun PlayerScoreDisplaysPrev() {
    TolleAppTheme {
        Surface {
            PlayerScoreDisplays(
                startValue = 301,
                isActive = true,
                playerState = PlayerState(
                    playerId = 1,
                    playerName = "Michi",
                    rounds = listOf(
                        listOf(
                            ThrowData(
                                fieldValue = 20,
                                isDouble = true,
                                isTriple = false,
                                throwIndex = 0
                            ),
                            ThrowData(
                                fieldValue = 20,
                                isDouble = false,
                                isTriple = false,
                                throwIndex = 1
                            ),
                            ThrowData(
                                fieldValue = 5,
                                isDouble = true,
                                isTriple = false,
                                throwIndex = 2
                            ),
                        )
                    )
                ),
                modifier = Modifier
                    .height(60.dp)
            )
        }
    }
}

@Composable
fun PlayerScoreDisplays(
    startValue: Int,
    isActive: Boolean,
    playerState: PlayerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = playerState.playerName,
                style = MaterialTheme.typography.headlineSmall,
                color = if (isActive) MaterialTheme.colorScheme.primary
                    else Color.Unspecified,

            )
        }
        VerticalDivider(Modifier.padding(horizontal = 2.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playerState.rounds.lastOrNull()?.getOrNull(0)?.calcScore()?.toString() ?: "",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                )
                VerticalDivider(Modifier.padding(horizontal = 2.dp))
                Text(
                    text = playerState.rounds.lastOrNull()?.getOrNull(1)?.calcScore()?.toString() ?: "",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                )
                VerticalDivider(Modifier.padding(horizontal = 2.dp))
                Text(
                    text = playerState.rounds.lastOrNull()?.getOrNull(2)?.calcScore()?.toString() ?: "",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 2.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = playerState.rounds.lastOrNull()?.sumOf { it.calcScore() }?.toString() ?: "—",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
        VerticalDivider(Modifier.padding(horizontal = 2.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val totalRoundPoints = playerState.rounds.sumOf { it.sumOf { throwData -> throwData.calcScore() } }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = (301 - totalRoundPoints).toString(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 2.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "∅ " + (totalRoundPoints / playerState.rounds.size.toFloat()).let {
                        if (it.isNaN()) "—" else String.format("%.2f", it)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        }
    }
}