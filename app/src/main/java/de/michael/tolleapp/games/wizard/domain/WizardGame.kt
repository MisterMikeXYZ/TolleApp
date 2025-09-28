package de.michael.tolleapp.games.wizard.domain

data class WizardGame(
    val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val roundsToPlay: Int,
    val playerIds: List<String> = emptyList(),
    val rounds: List<WizardRoundData> = emptyList(),
    val finished: Boolean = false,
)
