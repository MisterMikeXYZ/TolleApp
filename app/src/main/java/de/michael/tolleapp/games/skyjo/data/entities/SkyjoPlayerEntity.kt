package de.michael.tolleapp.games.skyjo.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.michael.tolleapp.games.util.player.Player

@Entity(
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = SkyjoGameEntity::class,
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
data class SkyjoPlayerEntity(
    val gameId: String,
    val playerId: String,
    val isWinner: Boolean,
    val isLoser: Boolean,
)