package de.michael.tolleapp.presentation.statistics

import de.michael.tolleapp.data.skyjo.stats.SkyjoStats

data class StatState(
    val players: List<SkyjoStats> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
)
