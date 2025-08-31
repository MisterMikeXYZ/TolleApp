package de.michael.tolleapp.presentation.schwimmen

data class SchwimmenState (
    val playerNames: Map<String, String> = emptyMap(),
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val selectedPlayerIds: List<String?> = listOf(null, null), // at least two slots
    val winnerId: List<String?> = listOf(null),
    val loserId: List<String?> = listOf(null),
    val ranking: List<String> = emptyList(),
    val isGameEnded: Boolean = false,
    val dealerIndex: Int = 0,
    val playerLives: Map<String, Int> = emptyMap(),
)