package de.michael.tolleapp.games.romme.data.mappers

import de.michael.tolleapp.games.romme.data.entities.RommeGameEntity
import de.michael.tolleapp.games.romme.domain.RommeGame
import de.michael.tolleapp.games.romme.domain.RommeRoundData

fun RommeGameEntity.toDomain(playerIds: List<String>, rounds: List<RommeRoundData>) = RommeGame(
    id = this.id,
    createdAt = this.createdAt,
    playerIds = playerIds,
    rounds = rounds,
    finished = this.finished
)