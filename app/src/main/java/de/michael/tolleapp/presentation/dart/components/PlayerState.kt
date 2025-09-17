package de.michael.tolleapp.presentation.dart.components

data class PlayerState (
    val playerId: String? = null,
    val playerName: String = "",
    val rounds: List<List<ThrowData>> = emptyList(),
)
