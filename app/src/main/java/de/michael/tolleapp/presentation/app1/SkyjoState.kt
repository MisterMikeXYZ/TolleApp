package de.michael.tolleapp.presentation.app1

import de.michael.tolleapp.data.Player

data class SkyjoState(
    val players: List<Player> = emptyList(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
)
