package de.michael.tolleapp.games.skyjo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.michael.tolleapp.games.util.PausedGame

@Entity(tableName = "skyjo_games")
data class SkyjoGame(
    @PrimaryKey override val id: String,
    override val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val dealerId: String? = null,
): PausedGame(id, createdAt)

@Entity(
    tableName = "skyjo_game_rounds",
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
data class SkyjoGameRound(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String,
    val roundIndex: Int,
    val roundScore: Int,
)

@Entity(
    tableName = "skyjo_game_winners",
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
data class SkyjoGameWinner(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String
)

@Entity(
    tableName = "skyjo_game_losers",
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
data class SkyjoGameLoser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String
)