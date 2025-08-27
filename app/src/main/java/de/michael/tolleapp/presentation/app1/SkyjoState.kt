package de.michael.tolleapp.presentation.app1

import de.michael.tolleapp.data.skyjo.player.SkyjoPlayer

data class SkyjoState(
    val players: List<SkyjoPlayer> = emptyList(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
    val perPlayerRounds: Map<String, List<Int>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winnerId: String? = null,
    val ranking: List<String> = emptyList(), // sorted playerIds by score
    val isGameEnded: Boolean = false,
)
