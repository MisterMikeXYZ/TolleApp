package de.michael.tolleapp.data.skyjo.stats

import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SkyjoStatsRepository(
    private val playerDao: PlayerDao,
    private val skyjoStatsDao: SkyjoStatsDao,
) {

    fun getPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: Player): Boolean {
        val existing = playerDao.getPlayerByName(player.name)
        if (existing != null) return false
        playerDao.insertPlayer(player)
        skyjoStatsDao.insertStats(SkyjoStats(playerId = player.id))
        return true
    }

    fun getAllStats(): Flow<List<SkyjoStats>> = skyjoStatsDao.getAllStatsFlow()

    suspend fun finalizePlayerStats(
        playerId: String,
        rounds: List<Int>,
        isWinner: Boolean,
        isLoser: Boolean
    ) {
        val stats = skyjoStatsDao.getStatsForPlayer(playerId) ?: SkyjoStats(playerId)
        val endScore = rounds.sum()

        // Round stats
        val bestRound = listOfNotNull(stats.bestRoundScoreSkyjo, rounds.minOrNull()).minOrNull()
        val worstRound = listOfNotNull(stats.worstRoundScoreSkyjo, rounds.maxOrNull()).maxOrNull()
        // End-of-game stats
        val bestEnd = listOfNotNull(stats.bestEndScoreSkyjo, endScore).minOrNull()
        val worstEnd = listOfNotNull(stats.worstEndScoreSkyjo, endScore).maxOrNull()

        val updated = stats.copy(
            bestRoundScoreSkyjo = bestRound,
            worstRoundScoreSkyjo = worstRound,
            bestEndScoreSkyjo = bestEnd,
            worstEndScoreSkyjo = worstEnd,
            roundsPlayedSkyjo = stats.roundsPlayedSkyjo + rounds.size,
            totalEndScoreSkyjo = stats.totalEndScoreSkyjo + endScore,
            totalGamesPlayedSkyjo = stats.totalGamesPlayedSkyjo + 1,
            wonGames = if (isWinner) stats.wonGames + 1 else stats.wonGames,
            lostGames = if (isLoser) stats.lostGames + 1 else stats.lostGames,
        )
        skyjoStatsDao.insertOrUpdateStats(updated)
    }

    suspend fun resetAllGameStats()
    {
        val statsList = skyjoStatsDao.getAllStats().first()
        statsList.forEach { stats ->
            val resetStats = stats.copy(
                bestRoundScoreSkyjo = null,
                worstRoundScoreSkyjo = null,
                bestEndScoreSkyjo = null,
                worstEndScoreSkyjo = null,
                roundsPlayedSkyjo = 0,
                totalGamesPlayedSkyjo = 0,
                totalEndScoreSkyjo = 0,
                wonGames = 0,
                lostGames = 0,
            )
            skyjoStatsDao.insertOrUpdateStats(resetStats)
        }
    }
}

