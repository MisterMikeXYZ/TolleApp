package de.michael.tolleapp.games.skyjo.presentation

data class SkyjoState(
    val playerNames: Map<String, String> = emptyMap(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null),
    val perPlayerRounds: Map<String, List<Int>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winnerId: List<String?> = listOf(null),
    val loserId: List<String?> = listOf(null),
    val ranking: List<String> = emptyList(),
    val isGameEnded: Boolean = false,
    val currentDealerId: String? = null,
    val neleModus: Boolean = false,
)