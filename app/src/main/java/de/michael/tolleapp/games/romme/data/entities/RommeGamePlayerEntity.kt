package de.michael.tolleapp.games.romme.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.michael.tolleapp.games.util.player.Player

@Entity(
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = RommeGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["gameId"]),
        Index(value = ["playerId"]),
    ]
)
data class RommeGamePlayerEntity(
    val gameId: String,
    val playerId: String
)
