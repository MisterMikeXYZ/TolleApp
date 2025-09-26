package de.michael.tolleapp.games.romme.domain

data class RommeRoundData(
    val roundNumber: Int,
    val roundScores: Map<String, Int?> = emptyMap(), // playerId to score in this round
    val finalScores: Map<String, Int?> = emptyMap(), // playerId to total score after this round
)
