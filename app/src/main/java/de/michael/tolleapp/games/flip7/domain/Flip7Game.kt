package de.michael.tolleapp.games.flip7.domain

data class Flip7Game (
    val gameId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val playerIds: List<String> = emptyList(),
    val rounds: List<Flip7RoundData> = emptyList(),
    val isFinished: Boolean = false,
    val dealerId: String? = null,
    val winnerId: String? = null,
)