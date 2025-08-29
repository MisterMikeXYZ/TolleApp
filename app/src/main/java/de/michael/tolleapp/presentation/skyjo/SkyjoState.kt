package de.michael.tolleapp.presentation.skyjo

data class SkyjoState(
    val playerNames: Map<String, String> = emptyMap(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
    val perPlayerRounds: Map<String, List<Int>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val visibleRoundRows: Int = 5,
    val winnerId: List<String?> = listOf(null),
    val loserId: List<String?> = listOf(null),
    val ranking: List<String> = emptyList(), // sorted playerIds by score
    val isGameEnded: Boolean = false,
    val dealerIndex: Int = 0,
)
