package de.michael.tolleapp.games.dart.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "dart_games")
data class DartGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val winnerId: String? = null,
    val gameStyle: Int? = null,
    val ranking: String? = null,
    val activePlayerIndex: Int = 0,
    val totalPlayers: Int = 0
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
    val isBust: Boolean,
)

@Entity(
    tableName = "dart_throws",
    foreignKeys = [
        ForeignKey(
            entity = DartGameRound::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roundId")]
)
data class DartThrowData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val throwIndex: Int,
    val fieldValue: Int,
    val isDouble: Boolean,
    val isTriple: Boolean,
)

data class RoundWithThrows(
    @Embedded val round: DartGameRound,
    @Relation(
        parentColumn = "id",
        entityColumn = "roundId"
    )
    val throws: List<DartThrowData>
)

data class GameWithRounds(
    @Embedded val game: DartGame,
    @Relation(
        entity = DartGameRound::class,
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val rounds: List<RoundWithThrows>
)