package de.michael.tolleapp.games.skyjo.presentation

import de.michael.tolleapp.games.util.table.SortDirection

data class SkyjoState(
    val playerNames: Map<String, String> = emptyMap(),
    val currentGameId: String = "",
    val selectedPlayerIds: List<String?> = listOf(null, null),
    val perPlayerRounds: Map<String, List<Int>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winnerId: List<String?> = listOf(null),
    val loserId: List<String?> = listOf(null),
    val ranking: List<String> = emptyList(),
    val isGameEnded: Boolean = false,
    val currentDealerId: String? = null,
    val lastKeyboardPage: Int = 0,

    val sortDirection: SortDirection = SortDirection.ASCENDING,
)