package de.michael.tolleapp.data.skyjo.game

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class SkyjoGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
)

@Entity(
    tableName = "game_rounds",
    foreignKeys = [
        ForeignKey(
            entity = SkyjoGame::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("playerId")]
)
data class GameRound(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String,
    val roundIndex: Int,
    val roundScore: Int
)
