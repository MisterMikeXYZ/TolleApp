package de.michael.tolleapp.games.romme.data.mappers

import de.michael.tolleapp.games.romme.data.entities.RommeRoundEntity
import de.michael.tolleapp.games.romme.domain.RommeRoundData
import kotlinx.serialization.json.Json

fun RommeRoundData.toEntity(gameId: String): RommeRoundEntity {
    return RommeRoundEntity(
        gameId = gameId,
        roundNumber = this.roundNumber,
        roundScores = Json.encodeToString(this.roundScores),
        finalScores = Json.encodeToString(this.finalScores),
    )
}

fun RommeRoundEntity.toDomain(): RommeRoundData {
    return RommeRoundData(
        roundNumber = this.roundNumber,
        roundScores = Json.decodeFromString(this.roundScores),
        finalScores = Json.decodeFromString(this.finalScores),
    )
}