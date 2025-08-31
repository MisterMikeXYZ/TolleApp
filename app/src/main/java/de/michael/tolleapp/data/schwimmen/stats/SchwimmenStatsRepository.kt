package de.michael.tolleapp.data.schwimmen.stats

import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import de.michael.tolleapp.data.skyjo.stats.SkyjoStats
import kotlinx.coroutines.flow.Flow

class SchwimmenStatsRepository(
    private val playerDao: PlayerDao,
    private val statsDao: SchwimmenStatsDao
) {

    suspend fun getStatsForPlayer(playerId: String): SchwimmenStats? =
        statsDao.getStatsForPlayer(playerId)

    //fun getPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()
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

    suspend fun loseLife(playerId: String, currentLives: Int): Int {
        val newLives = (currentLives - 1).coerceAtLeast(0)
        return newLives
    }

    fun checkGameOver(playerLives: Map<String, Int>): Boolean {
        val alivePlayers = playerLives.filter { it.value > 0 }
        return alivePlayers.size <= 1
    }

    fun getRoundResults(playerLives: Map<String, Int>): RoundResult {
        val losers = playerLives.filter { it.value == 0 }.keys.toList()
        val alive = playerLives.filter { it.value > 0 }.keys.toList()
        val winners = if (alive.size == 1) alive else emptyList()
        return RoundResult(winners = winners, losers = losers)
    }

    data class RoundResult(
        val winners: List<String>,
        val losers: List<String>
    )
}
