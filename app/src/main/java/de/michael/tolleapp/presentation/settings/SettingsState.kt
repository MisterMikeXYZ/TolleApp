package de.michael.tolleapp.presentation.settings

import de.michael.tolleapp.data.player.Player

data class SettingsState(
    val isDarkmode: Boolean = false,
    val players: List<Player> = emptyList(),
    val playersToDelete: List<Player> = emptyList()
)