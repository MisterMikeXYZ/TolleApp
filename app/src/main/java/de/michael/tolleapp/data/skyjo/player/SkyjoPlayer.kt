package de.michael.tolleapp.data.skyjo.player

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "players")
data class SkyjoPlayer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bestRoundScoreSkyjo: Int? = null,
    val worstRoundScoreSkyjo: Int? = null,
    val bestEndScoreSkyjo: Int? = null,
    val worstEndScoreSkyjo: Int? = null,
    val roundsPlayedSkyjo: Int = 0,
    val totalGamesPlayedSkyjo: Int = 0,
    val totalRoundScoreSkyjo: Int = 0,
    val totalEndScoreSkyjo: Int = 0
)

@Entity(tableName = "round_results")
data class RoundResult(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val gameId: String,
    val roundScore: Int,
)