package de.michael.tolleapp.games.wizard.data.mappers

import de.michael.tolleapp.games.wizard.data.entities.WizardRoundEntity
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import kotlinx.serialization.json.Json

fun WizardRoundData.toEntity(gameId: String): WizardRoundEntity {
    return WizardRoundEntity(
        gameId = gameId,
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        bids = Json.encodeToString(this.bids),
        bidsFinal = this.bidsFinal,
        tricksWon = Json.encodeToString(this.tricksWon),
        scores = Json.encodeToString(this.scores),
    )
}

fun WizardRoundEntity.toDomain(): WizardRoundData {
    return WizardRoundData(
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        bids = Json.decodeFromString(this.bids),
        bidsFinal = this.bidsFinal,
        tricksWon = Json.decodeFromString(this.tricksWon),
        scores = Json.decodeFromString(this.tricksWon),
    )
}