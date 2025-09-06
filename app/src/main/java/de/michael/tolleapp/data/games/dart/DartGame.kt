package de.michael.tolleapp.data.games.dart

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dart_games")
data class DartGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val winnerId: String? = null,
    val gameStyle: Int? = null,
)

@Entity(
    tableName = "dart_game_rounds",
    foreignKeys = [
        ForeignKey(
            entity = DartGame::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("playerId")]
)
data class DartGameRound(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String,
    val roundIndex: Int,
    val dart1: Int,
    val dart2: Int,
    val dart3: Int,
) {
    val total: Int get() = dart1 + dart2 + dart3
}