package de.michael.tolleapp.games.wizard.domain

data class WizardRoundData(
    val roundNumber: Int,
    val dealerId: String?,
    val bids: Map<String, Int?> = emptyMap(), // playerId to bid
    val bidsFinal: Boolean = false,
    val tricksWon: Map<String, Int?> = emptyMap(), // playerId to tricks won
    val scores: Map<String, Int> = emptyMap(), // playerId to score after this round
)