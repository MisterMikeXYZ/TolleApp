package de.michael.tolleapp.games.dart.presentation

import de.michael.tolleapp.games.dart.presentation.components.PlayerState
import de.michael.tolleapp.games.dart.presentation.components.ThrowData

data class DartState(

    // --- Players ---
    val playerNames: Map<String, String> = emptyMap(),
    val selectedPlayerIds: List<String?> = listOf(null, null),

    // --- Game info ---
    val currentGameId: String = "",
    val activePlayerIndex: Int = 0,
    val gameStyle: Int = 301,

    // --- Rounds and scoring ---
    val perPlayerRounds: Map<String, List<List<ThrowData>>> = emptyMap(),

    // --- Game result ---
    val winnerId: String? = null,
    val loserId: String? = null,
    val ranking: List<String> = emptyList(),

    // --- UI / state ---
    val isGameEnded: Boolean = false,
)

fun DartState.toPlayerState(playerId: String): PlayerState {
    return PlayerState(
        playerId = playerId,
        playerName = playerNames[playerId] ?: "",
        hasFinished = ranking.contains(playerId),
        position = ranking.indexOf(playerId) + 1,
        rounds = perPlayerRounds[playerId] ?: emptyList()
    )
}