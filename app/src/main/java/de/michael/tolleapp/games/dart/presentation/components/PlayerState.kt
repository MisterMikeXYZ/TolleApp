package de.michael.tolleapp.games.dart.presentation.components

data class PlayerState (
    val playerId: String? = null,
    val playerName: String = "",
    val hasFinished: Boolean = false,
    val position: Int? = null,
    val rounds: List<List<ThrowData>> = emptyList(),
)
