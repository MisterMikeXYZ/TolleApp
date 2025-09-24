package de.michael.tolleapp.games.wizard.data.mappers

import de.michael.tolleapp.games.wizard.data.entities.WizardGameEntity
import de.michael.tolleapp.games.wizard.domain.WizardGame
import de.michael.tolleapp.games.wizard.domain.WizardRoundData

fun WizardGameEntity.toDomain(
    playerIds: List<String>,
    rounds: List<WizardRoundData>,
): WizardGame {
    return WizardGame(
        id = this.id,
        createdAt = this.createdAt,
        roundsToPlay = this.roundsToPlay,
        playerIds = playerIds,
        rounds = rounds,
        finished = this.finished,
    )
}