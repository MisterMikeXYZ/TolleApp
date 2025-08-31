package de.michael.tolleapp.data.schwimmen.stats

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGame
import java.util.UUID

@Entity(tableName = "schwimmen_stats")
data class SchwimmenStats(
    @PrimaryKey val playerId: String,
    val bestEndScoreSchwimmen: Int? = null,
    val roundsPlayedSchwimmen: Int = 0,
    val totalGamesPlayedSchwimmen: Int = 0,
    val wonGames: Int = 0,
    val firstOutGames: Int = 0,
)

@Entity(
    tableName = "schwimmen_game_players",
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
data class SchwimmenGamePlayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val playerId: String,
    val livesRemaining: Int = 3,
    val isOut: Boolean = false,
)