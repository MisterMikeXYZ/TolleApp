package de.michael.tolleapp.statistics.gameStats

data class SkyjoStats(
    val playerId: String,
    val totalGames: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val roundsPlayed: Int = 0,
    val bestRound: Int? = null,
    val worstRound: Int? = null,
    val avgRound: Double? = null,
    val bestEnd: Int? = null,
    val worstEnd: Int? = null,
    val totalEnd: Int? = null,
)