package de.michael.tolleapp.settings.presentation

import de.michael.tolleapp.games.util.player.Player

data class SettingsState(
    val isDarkmode: Boolean = false,
    val players: List<Player> = emptyList(),
    val playersToDelete: List<Player> = emptyList()
)