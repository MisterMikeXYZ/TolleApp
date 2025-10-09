package de.michael.tolleapp.statistics

import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.statistics.gameStats.DartStats
import de.michael.tolleapp.statistics.gameStats.Flip7Stats
import de.michael.tolleapp.statistics.gameStats.SkyjoStats

data class StatState(
    val playersSkyjo: List<SkyjoStats> = emptyList(),
    val playersSchwimmen: List<SchwimmenStats> = emptyList(),
    val playersFlip7: List<Flip7Stats> = emptyList(),
    val playersDart: List<DartStats> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
    val selectedGame: GameType? = null,
)