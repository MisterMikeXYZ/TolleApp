package de.michael.tolleapp.data.schwimmen.game

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "schwimmen_games")
data class SchwimmenGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val screenType: GameScreenType,
)

enum class GameScreenType {
    CIRCLE,
    CANVAS
}

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
    indices = [Index("gameId")]
)
data class SchwimmenGameRound(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String,
    val roundIndex: Int,
    val lives: Int,
)