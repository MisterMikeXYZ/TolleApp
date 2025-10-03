package de.michael.tolleapp.games.flip7.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.michael.tolleapp.games.util.player.Player

@Entity(
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = Flip7GameEntity::class,
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
    indices = [Index("gameId"), Index("playerId")]
)
data class Flip7PlayerEntity(
    val gameId: String,
    val playerId: String,
    val index: Int,
)