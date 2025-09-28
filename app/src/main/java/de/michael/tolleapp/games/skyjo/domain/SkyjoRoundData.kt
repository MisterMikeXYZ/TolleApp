package de.michael.tolleapp.games.skyjo.domain

class SkyjoRoundData (
    val roundNumber: Int,
    val dealerId: String?,
    val scores: Map<String, Int> = emptyMap(),
)