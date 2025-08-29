package de.michael.tolleapp.data.skyjo.player

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "skyjo_stats")
data class SkyjoStats(
    @PrimaryKey val playerId: String,
    val bestRoundScoreSkyjo: Int? = null,
    val worstRoundScoreSkyjo: Int? = null,
    val bestEndScoreSkyjo: Int? = null,
    val worstEndScoreSkyjo: Int? = null,
    val roundsPlayedSkyjo: Int = 0,
    val totalGamesPlayedSkyjo: Int = 0,
    val totalEndScoreSkyjo: Int = 0,
    val wonGames: Int = 0,
    val lostGames: Int = 0,
)

@Entity(tableName = "round_results")
data class RoundResult(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val gameId: String,
    val roundScore: Int,
)