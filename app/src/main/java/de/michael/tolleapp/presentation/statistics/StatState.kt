package de.michael.tolleapp.presentation.statistics

import de.michael.tolleapp.data.skyjo.player.SkyjoPlayer

data class StatState(
    val players: List<SkyjoPlayer> = emptyList(),
    //val games : List<Games> = emptyList(),
)
