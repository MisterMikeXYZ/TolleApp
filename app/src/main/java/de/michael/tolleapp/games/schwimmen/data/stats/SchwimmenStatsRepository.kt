package de.michael.tolleapp.games.schwimmen.data.stats

import de.michael.tolleapp.games.player.Player
import de.michael.tolleapp.games.player.PlayerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchwimmenStatsRepository(
    private val playerDao: PlayerDao,
    private val statsDao: SchwimmenStatsDao
) {
    // expose players so the VM can observe names just like Skyjo
    fun getPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: Player): Boolean {
        val existing = playerDao.getPlayerByName(player.name)
        if (existing != null) return false
        playerDao.insertPlayer(player)
        statsDao.insertStats(SchwimmenStats(playerId = player.id))
        return true
    }

    fun getAllStats(): Flow<List<SchwimmenStats>> = statsDao.getAllStatsFlow()

    suspend fun finalizePlayerStats(
        playerId: String,
        lives: Int,
        isWinner: Boolean,
        firstOut: Boolean,
        rounds: Int,
    ) {
        val stats = statsDao.getStatsForPlayer(playerId) ?: SchwimmenStats(playerId)
        val bestEnd = listOfNotNull((stats.bestEndScoreSchwimmen), lives).maxOrNull()

        val updated = stats.copy(
            bestEndScoreSchwimmen = bestEnd,
            roundsPlayedSchwimmen = stats.roundsPlayedSchwimmen + rounds,
            totalGamesPlayedSchwimmen = stats.totalGamesPlayedSchwimmen + 1,
            wonGames = if (isWinner) stats.wonGames + 1 else stats.wonGames,
            firstOutGames = if (firstOut) stats.firstOutGames + 1 else stats.firstOutGames,
        )
        statsDao.insertOrUpdateStats(updated)
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
            statsDao.insertOrUpdateStats(resetStats)
        }
    }
}