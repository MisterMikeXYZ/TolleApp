package de.michael.tolleapp.games.flip7.domain

data class Flip7RoundData (
    val roundNumber: Int,
    val dealerId: String?,
    val scores: Map<String, Int> = emptyMap(),
)