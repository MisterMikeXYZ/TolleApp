package de.michael.tolleapp.games.skyjo.data.mappers

import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.domain.SkyjoGame
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData

fun SkyjoGameEntity.toDomain(
    playerIds: List<String>,
    rounds: List<SkyjoRoundData>,
): SkyjoGame {
    return SkyjoGame(
        id = this.id,
        createdAt = this.createdAt,
        playerIds = playerIds,
        rounds = rounds,
        finished = this.isFinished,
        dealerId = this.dealerId,
        endPoints = this.endPoints,
    )
}