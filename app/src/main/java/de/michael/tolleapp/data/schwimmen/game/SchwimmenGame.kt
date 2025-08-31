package de.michael.tolleapp.data.schwimmen.game

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.michael.tolleapp.data.skyjo.game.SkyjoGame

@Entity(tableName = "schwimmen_games")
data class SchwimmenGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
)

@Entity(
    tableName = "schwimmen_game_rounds",
    foreignKeys = [
        ForeignKey(
            entity = SchwimmenGame::class,
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