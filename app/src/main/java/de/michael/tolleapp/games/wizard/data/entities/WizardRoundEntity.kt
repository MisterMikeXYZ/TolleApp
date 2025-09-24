package de.michael.tolleapp.games.wizard.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.michael.tolleapp.games.util.player.Player

@Entity(
    primaryKeys = ["gameId", "roundNumber"],
    foreignKeys = [
        ForeignKey(
            entity = WizardGameEntity::class,
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
    indices = [
        Index(value = ["gameId"]),
        Index(value = ["dealerId"]),
    ]
)
data class WizardRoundEntity(
    val gameId: String,
    val roundNumber: Int,
    val dealerId: String?,
    val bids: String, // JSON-encoded map of playerId to bid
    val bidsFinal: Boolean = false,
    val tricksWon: String, // JSON-encoded map of playerId to tricks won
    val scores: String, // JSON-encoded map of playerId to score after this round
)