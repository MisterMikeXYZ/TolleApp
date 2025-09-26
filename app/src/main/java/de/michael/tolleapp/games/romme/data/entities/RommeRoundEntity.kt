package de.michael.tolleapp.games.romme.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["gameId", "roundNumber"],
    foreignKeys = [
        ForeignKey(
            entity = RommeGameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["gameId"]),
    ]
)
data class RommeRoundEntity(
    val gameId: String,
    val roundNumber: Int,
    val roundScores: String, // JSON encoded playerId to score in this round
    val finalScores: String, // JSON encoded playerId to total score after this round
)
