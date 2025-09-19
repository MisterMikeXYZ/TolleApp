package de.michael.tolleapp.settings.presentation.settings

import de.michael.tolleapp.games.player.Player

data class SettingsState(
    val isDarkmode: Boolean = false,
    val players: List<Player> = emptyList(),
    val playersToDelete: List<Player> = emptyList()
)