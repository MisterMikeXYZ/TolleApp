package de.michael.tolleapp.games.skyjo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.skyjo.presentation.SkyjoState
import de.michael.tolleapp.games.util.player.Player

@Preview
@Composable
fun SkyjoPlayerDisplayRowPreview() {
    val round1 = SkyjoRoundData(
        roundNumber = 1,
        dealerId = "1",
        scores = mapOf(
            "1" to 10,
            "2" to 20,
        )
    )
    val round2 = SkyjoRoundData(
        roundNumber = 2,
        dealerId = "2",
        scores = mapOf(
            "1" to 30,
            "2" to 40,
        )
    )
    val rounds = listOf(round1, round2)
    val state = SkyjoState(
        currentDealerId = "1",
        selectedPlayers = listOf(Player("1", "Anna"), Player("2", "Ben"), Player("3", "Clara")),
        rounds = rounds,
        //Sum of all scores in rounds for each player
        totalPoints = mapOf(
            "1" to rounds.sumOf { it.scores["1"] ?: 0 },
            "2" to rounds.sumOf { it.scores["2"] ?: 0 },
            "3" to rounds.sumOf { it.scores["3"] ?: 0 },
        )
    )
    MaterialTheme {
//        SkyjoPlayerDisplayRow(
//            playerId = "1",
//            state = state,
//            onClick = {},
//            isActivePlayer = true,
//        )
    }
}


@Composable
fun SkyjoPlayerDisplayRow(
    player: Player,
    isDealer: Boolean,
    roundScore: Int?,
    totalScore: Int?,
    onClick: () -> Unit,
    isActivePlayer: Boolean,
) {
    val backgroundColor =
        if (isActivePlayer) MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        )
        else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = true) {
                onClick()
            }
            .padding(4.dp)
    ) {
        BetterOutlinedTextField(
            value = player.name,
            modifier = Modifier.weight(1f).padding(top = 3.dp),
            textColor = if (isDealer) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
            textStyle = if (isDealer) MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp
            )
            else LocalTextStyle.current,
            readOnly = false
        )

        Spacer(modifier = Modifier.width(12.dp))

        BetterOutlinedTextField(
            // read the last value from rounds
            value = roundScore?.toString() ?: "",
            label = "Punkte",
            modifier = Modifier
                .weight(1f).padding(top = 5.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        BetterOutlinedTextField(
            value = totalScore?.toString() ?: "",
            label = "Gesamt",
            modifier = Modifier.weight(1f).padding(top = 3.dp),
            readOnly = true
        )
    }
}