package de.michael.tolleapp.statistics.gameStats

data class DartStats(
    // General --------------------------------------------------------------------
    val playerId: String,
    val roundsPlayed: Int = 0,
    val highestThrow: Int? = null,

    // Game ----------------------------------------------------------------------------
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val winRate: Double = 0.0,

    // High Rounds ---------------------------------------------------------------------
    val over60Rounds: Int? = null,
    val over100Rounds: Int? = null,
    val over140Rounds: Int? = null,
    val perfectRounds: Int? = null,


    // Throws ----------------------------------------------------------------------
    val dartsThrown: Int = 0,
    val average3Darts: Int? = null,
    val first9Average: Int? = null,
    val highestRound: Int? = null,
    val allPoints: Int = 0,
    val dart1Average: Int? = null,
    val dart2Average: Int? = null,
    val dart3Average: Int? = null,


    // Throwrates ---------------------------------------------------------------------
    val tripleRate: Double? = null,
    val doubleRate: Double? = null,
    val triple10Rate: Double? = null,
    val bullsRate: Double? = null,
    val doubleBullsRate: Double? = null,
    val miss: Double? = null,
    val one: Double? = null,
    val two: Double? = null,
    val three: Double? = null,
    val four: Double? = null,
    val five: Double? = null,
    val six: Double? = null,
    val seven: Double? = null,
    val eight: Double? = null,
    val nine: Double? = null,
    val ten: Double? = null,
    val eleven: Double? = null,
    val twelve: Double? = null,
    val thirteen: Double? = null,
    val fourteen: Double? = null,
    val fifteen: Double? = null,
    val sixteen: Double? = null,
    val seventeen: Double? = null,
    val eighteen: Double? = null,
    val nineteen: Double? = null,
    val twenty: Double? = null,
    val bull: Double? = null,

    // Checkout -------------------------------------------------------
    val highestCheckout: Int? = null,
    val minDarts: Int? = null,
)