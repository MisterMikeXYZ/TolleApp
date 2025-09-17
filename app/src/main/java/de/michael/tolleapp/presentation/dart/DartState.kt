package de.michael.tolleapp.presentation.dart

import de.michael.tolleapp.presentation.dart.components.PlayerState
import de.michael.tolleapp.presentation.dart.components.ThrowData

data class DartState(

    // --- Players ---
    val playerNames: Map<String, String> = emptyMap(),
    val selectedPlayerIds: List<String?> = listOf(null, null),

    // --- Game info ---
    val currentGameId: String = "",
    val currentGameRounds: Int = 1,
    val activePlayerIndex: Int = 0,
    val gameStyle: Int = 301,

    // --- Rounds and scoring ---
    val perPlayerRounds: Map<String, List<List<ThrowData>>> = emptyMap(),
    val totalPoints: Map<String, Int> = emptyMap(),
    val bestRound: Map<String, Int> = emptyMap(),
    val worstRound: Map<String, Int> = emptyMap(),
    val perfectRounds: Map<String, Int> = emptyMap(),
    val tripleTwentyHits: Map<String, Int> = emptyMap(),

    // --- Game result ---
    val winnerId: String? = null,
    val loserIds: List<String?> = emptyList(),
    val ranking: List<String> = emptyList(),

    // --- UI / state ---
    val isGameEnded: Boolean = false,
)

fun DartState.toPlayerState(playerId: String): PlayerState {
    return PlayerState(
        playerId = playerId, // only if playerId is numeric
        playerName = playerNames[playerId] ?: "",
        rounds = perPlayerRounds[playerId] ?: emptyList()
    )
}