package de.michael.tolleapp.presentation.statistics

import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.skyjo.stats.SkyjoStats

enum class GameType { SKYJO, SCHWIMMEN }

data class StatState(
    val playersSkyjo: List<SkyjoStats> = emptyList(),
    val playersSchwimmen: List<SchwimmenStats> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
    val selectedGame: GameType = GameType.SKYJO
)