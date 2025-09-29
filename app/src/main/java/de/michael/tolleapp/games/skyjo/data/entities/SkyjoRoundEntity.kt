package de.michael.tolleapp.games.skyjo.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.michael.tolleapp.games.util.player.Player

@Entity(
    primaryKeys = ["gameId", "roundNumber"],
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
            childColumns = ["dealerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("gameId"), Index("dealerId")]
)
data class SkyjoRoundEntity(
    val gameId: String,
    val roundNumber: Int,
    val dealerId: String?,
    val scores: String, // JSON-encoded map of playerId to score after this round
)