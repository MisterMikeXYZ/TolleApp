package de.michael.tolleapp.data.games.skyjo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "skyjo_games")
data class SkyjoGame(
    @PrimaryKey val id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val isFinished: Boolean = false,
    val dealerId: String? = null,
)

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