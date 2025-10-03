package de.michael.tolleapp.games.flip7.data.mappers

import de.michael.tolleapp.games.flip7.data.entities.Flip7RoundEntity
import de.michael.tolleapp.games.flip7.domain.Flip7RoundData
import kotlinx.serialization.json.Json

fun Flip7RoundData.toEntity(gameId: String): Flip7RoundEntity {
    return Flip7RoundEntity(
        gameId = gameId,
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        scores = Json.encodeToString(this.scores),
    )
}

fun Flip7RoundEntity.toDomain(): Flip7RoundData {
    return Flip7RoundData(
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        scores = Json.decodeFromString(this.scores),
    )
}