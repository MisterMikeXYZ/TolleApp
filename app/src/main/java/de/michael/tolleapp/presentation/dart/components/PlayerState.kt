package de.michael.tolleapp.presentation.dart.components

data class PlayerState (
    val playerId: Int,
    val playerName: String,
    val rounds: List<List<ThrowData>>,
)
