package de.michael.tolleapp.statistics.gameStats

data class SchwimmenStats(
    val playerId: String,
    val totalGames: Int = 0,
    val roundsPlayed: Int = 0,
    val bestEndscore: Int? = null,
    val wonGames: Int = 0,
    val firstOutGames: Int = 0
)