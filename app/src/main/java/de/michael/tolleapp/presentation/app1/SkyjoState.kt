package de.michael.tolleapp.presentation.app1

import de.michael.tolleapp.data.SkyjoPlayer

data class SkyjoState(
    val players: List<SkyjoPlayer> = emptyList(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
    // Map which stores all rounds per player ID
    //val playerRounds: Map<String, List<Int>> = emptyMap()
)
