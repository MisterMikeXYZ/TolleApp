package de.michael.tolleapp.statistics

import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.statistics.gameStats.SkyjoStats

enum class GameType { SKYJO, SCHWIMMEN }

data class StatState(
    val playersSkyjo: List<SkyjoStats> = emptyList(),
    val playersSchwimmen: List<SchwimmenStats> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
    val selectedGame: GameType? = null,
)