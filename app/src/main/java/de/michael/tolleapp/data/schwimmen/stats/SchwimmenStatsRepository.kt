package de.michael.tolleapp.data.schwimmen.stats

import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchwimmenStatsRepository(
    private val playerDao: PlayerDao,
    private val statsDao: SchwimmenStatsDao
) {

    suspend fun getStatsForPlayer(playerId: String): SchwimmenStats? =
        statsDao.getStatsForPlayer(playerId)

    // expose players so the VM can observe names just like Skyjo
    fun getPlayers(): kotlinx.coroutines.flow.Flow<List<Player>> = playerDao.getAllPlayers()


    fun getAllStats(): Flow<List<SchwimmenStats>> =
        statsDao.getAllStatsFlow()

    suspend fun ensureStatsExists(playerId: String) {
        val existing = statsDao.getStatsForPlayer(playerId)
        if (existing == null) {
            statsDao.insertStats(SchwimmenStats(playerId = playerId))
        }
    }

    suspend fun addPlayer(player: Player): Boolean {
        val existing = playerDao.getPlayerByName(player.name)
        if (existing != null) return false
        playerDao.insertPlayer(player)
        // also create empty stats for Skyjo
        statsDao.insertStats(SchwimmenStats(playerId = player.id))
        return true
    }

    suspend fun recordRoundPlayed(playerId: String) {
        val stats = statsDao.getStatsForPlayer(playerId) ?: return
        statsDao.updateStats(stats.copy(
            roundsPlayedSchwimmen = stats.roundsPlayedSchwimmen + 1
        ))
    }

    suspend fun recordGamePlayed(playerId: String, won: Boolean, firstOut: Boolean) {
        val stats = statsDao.getStatsForPlayer(playerId)
        if (stats != null) {
            statsDao.updateStats(
                stats.copy(
                    totalGamesPlayedSchwimmen = stats.totalGamesPlayedSchwimmen + 1,
                    wonGames = stats.wonGames + if (won) 1 else 0,
                    firstOutGames = stats.firstOutGames + if (firstOut) 1 else 0
                )
            )
        } else {
            statsDao.insertStats(
                SchwimmenStats(
                    playerId = playerId,
                    totalGamesPlayedSchwimmen = 1,
                    wonGames = if (won) 1 else 0,
                    firstOutGames = if (firstOut) 1 else 0
                )
            )
        }
    }

    suspend fun resetAllGameStats()
    {
        val statsList = statsDao.getAllStats().first()
        statsList.forEach { stats ->
            val resetStats = stats.copy(
                bestEndScoreSchwimmen = null,
                roundsPlayedSchwimmen = 0,
                totalGamesPlayedSchwimmen = 0,
                wonGames = 0,
                firstOutGames = 0,
            )
            statsDao.updateStats(resetStats)
        }
    }

    data class RoundResult(
        val winners: List<String>,
        val losers: List<String>
    )
}