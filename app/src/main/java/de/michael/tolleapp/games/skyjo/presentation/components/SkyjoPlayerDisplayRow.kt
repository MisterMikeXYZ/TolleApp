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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.games.skyjo.presentation.SkyjoState

@Composable
fun SkyjoPlayerDisplayRow(
    playerId: String,
    state: SkyjoState,
    points: Map<String, String>,
    totalPoints: Map<String, Int>,
    isActivePlayer: Boolean,
    onClick: () -> Unit,
) {
    val playerName = state.playerNames[playerId] ?: "Spieler auswählen"
    val isDealer = playerId == state.currentDealerId

    val backgroundColor =
        if (isActivePlayer) MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        )
        else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = true) {
                onClick()
            }
            .padding(4.dp)
    ) {
        BetterOutlinedTextField(
            value = playerName,
            modifier = Modifier.weight(1f),
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
            value = points[playerId] ?: "",
            label = "Punkte",
            modifier = Modifier
                .weight(1f),
        )

        Spacer(modifier = Modifier.width(12.dp))

        BetterOutlinedTextField(
            value = (totalPoints[playerId] ?: 0).toString(),
            label = "Gesamt",
            modifier = Modifier.weight(1f),
            readOnly = true
        )
    }
}