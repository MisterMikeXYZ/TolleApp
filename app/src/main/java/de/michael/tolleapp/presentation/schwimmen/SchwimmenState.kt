package de.michael.tolleapp.presentation.schwimmen

import de.michael.tolleapp.data.schwimmen.game.GameScreenType

data class SchwimmenState (
    val playerNames: Map<String, String> = emptyMap(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 0,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
    val perPlayerRounds: Map<String, Int> = emptyMap(), //Id and lives left
    val winnerId: String? = null,
    val loserId: String? = null,
    val losers: List<String?> = emptyList(),
    val ranking: List<String> = emptyList(),
    val isGameEnded: Boolean = false,
    val dealerIndex: Int = 0,
    val screenType: GameScreenType = GameScreenType.CIRCLE,
)