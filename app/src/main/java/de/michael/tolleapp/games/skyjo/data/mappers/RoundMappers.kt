package de.michael.tolleapp.games.skyjo.data.mappers

import de.michael.tolleapp.games.skyjo.data.entities.SkyjoRoundEntity
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import kotlinx.serialization.json.Json

fun SkyjoRoundData.toEntity(gameId: String): SkyjoRoundEntity {
    return SkyjoRoundEntity(
        gameId = gameId,
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        scores = Json.encodeToString(this.scores),
    )
}

fun SkyjoRoundEntity.toDomain(): SkyjoRoundData {
    return SkyjoRoundData(
        roundNumber = this.roundNumber,
        dealerId = this.dealerId,
        scores = Json.decodeFromString(this.scores),
    )
}