package de.michael.tolleapp.games.flip7.data.mappers

import de.michael.tolleapp.games.flip7.data.entities.Flip7GameEntity
import de.michael.tolleapp.games.flip7.domain.Flip7Game
import de.michael.tolleapp.games.flip7.domain.Flip7RoundData

fun Flip7GameEntity.toDomain(
    playerIds: List<String>,
    rounds: List<Flip7RoundData>,
): Flip7Game {
    return Flip7Game(
        gameId = this.id,
        createdAt = this.createdAt,
        playerIds = playerIds,
        rounds = rounds,
        isFinished = this.isFinished,
        dealerId = this.dealerId,
        winnerId = this.winnerId,
    )
}