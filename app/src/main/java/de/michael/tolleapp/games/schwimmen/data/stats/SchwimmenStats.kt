package de.michael.tolleapp.games.schwimmen.data.stats

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schwimmen_stats")
data class SchwimmenStats(
    @PrimaryKey val playerId: String,
    val bestEndScoreSchwimmen: Int? = null,
    val roundsPlayedSchwimmen: Int = 0,
    val totalGamesPlayedSchwimmen: Int = 0,
    val wonGames: Int = 0,
    val firstOutGames: Int = 0,
)