package de.michael.tolleapp.games.dart.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.R
import de.michael.tolleapp.ui.theme.AppTheme

@Preview
@Composable
private fun PlayerScoreDisplaysPrev() {
    AppTheme {
        Surface {
            PlayerScoreDisplays(
                startValue = 50,
                isActive = true,
                playerState = PlayerState(
                    playerId = "1",
                    playerName = "Nicolas",
                    rounds = listOf(
                        listOf(
                            ThrowData(
                                fieldValue = 20,
                                isDouble = true,
                                isTriple = false,
                                throwIndex = 0,
                                isBust = false,
                            ),
                            ThrowData(
                                fieldValue = 20,
                                isDouble = false,
                                isTriple = false,
                                throwIndex = 1,
                                isBust = false,
                                ),
                            ThrowData(
                                fieldValue = 5,
                                isDouble = true,
                                isTriple = false,
                                throwIndex = 2,
                                isBust = false,
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
    val lastRound = playerState.rounds.lastOrNull() ?: emptyList()

    val roundsPlayed = playerState.rounds.sumOf { it.size }

    val displayRound: List<String> = if (lastRound.isNotEmpty()) {
        List(3) { i ->
            val throwData = lastRound.getOrNull(i)
            when {
                throwData == null -> "" // unused throw
                throwData.isBust -> throwData.displayValue()
                else -> throwData.calcScore()?.toString() ?: ""
            }
        }
    } else {
        listOf("", "", "")
    }
    val lastRoundSum = if (lastRound.any { it.isBust }) {
        "Bust"
    } else {
        lastRound.sumOf { it.calcScore() ?: 0 }.toString()
    }

    val animatable = remember { Animatable(initialValue = 24f) }

    LaunchedEffect(isActive) {
        if(isActive) {
            animatable.animateTo(
                targetValue = 32f,
            )
        } else {
            animatable.animateTo(
                targetValue = 24f,
            )
        }
    }
    Row(
        modifier
    ) {
        // --- Name ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ){
                Text(
                    text = playerState.playerName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = animatable.value.sp),
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else Color.Unspecified,
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ){
                    Text(
                        text = roundsPlayed.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_dart),
                        contentDescription = "Dart icon",
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        VerticalDivider(Modifier.padding(horizontal = 2.dp))

        // --- Points ---
        if (playerState.hasFinished) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DONE: " + playerState.position.toString() + ".",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    for (i in 0..2) {
                        Text(
                            text = displayRound.getOrElse(i) { "" },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (lastRound.getOrNull(i)?.isBust == true)
                                    MaterialTheme.colorScheme.error
                                else
                                    Color.Unspecified
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (i < 2) VerticalDivider(Modifier.padding(horizontal = 2.dp))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 2.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = lastRoundSum,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }
        VerticalDivider(Modifier.padding(horizontal = 2.dp))

        // --- Totals ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
        ) {
            val totalRoundPoints = playerState.rounds.sumOf { round ->
                round.mapNotNull { it.calcScore() }.sum()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(
                    text = (startValue - totalRoundPoints).toString(),
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
                        if (it.isNaN()) "0.0" else String.format("%.2f", it)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        }
    }
}