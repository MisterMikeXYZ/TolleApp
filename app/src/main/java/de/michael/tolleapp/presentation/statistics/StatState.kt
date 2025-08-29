package de.michael.tolleapp.presentation.statistics

import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.skyjo.player.SkyjoStats

data class StatState(
    val players: List<SkyjoStats> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
)
