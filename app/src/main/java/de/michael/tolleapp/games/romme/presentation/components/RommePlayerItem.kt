package de.michael.tolleapp.games.romme.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.BetterOutlinedTextField

@Composable
fun RommePlayerItem(
    playerName: String,
    roundScore: Int?,
    totalScore: Int?,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    clickEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .then(
                if (highlighted) Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                else Modifier
            )
            .then(
                if (onClick != null) Modifier
                    .clickable(
                        enabled = clickEnabled,
                        onClick = onClick,
                    )
                else Modifier
            )
            .padding(4.dp)
    ) {
        Text(
            text = playerName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(0.3f),
        )
        BetterOutlinedTextField(
            value = roundScore?.toString() ?: "",
            label = "Runde",
            modifier = Modifier
                .padding(vertical = 4.dp)
                .weight(0.3f)
        )
        BetterOutlinedTextField(
            value = totalScore?.toString() ?: "",
            label = "Gesamt",
            modifier = Modifier
                .padding(vertical = 4.dp)
                .weight(0.3f)
        )
    }
}