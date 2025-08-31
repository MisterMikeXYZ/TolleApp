package de.michael.tolleapp.data.schwimmen.game

import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsDao
import kotlinx.coroutines.flow.Flow

class SchwimmenGameRepository (
    private val gameDao: SchwimmenGameDao,
    private val statsDao: SchwimmenStatsDao

) {
    suspend fun createGame(gameId: String): SchwimmenGame {
        val game = SchwimmenGame(id = gameId)
        gameDao.insertGame(game)
        return game
    }

    suspend fun getGameById(gameId: String): SchwimmenGame? =
        gameDao.getGameById(gameId)

    fun getAllGames(): Flow<List<SchwimmenGame>> =
        gameDao.getAllGames()

    suspend fun finishGame(gameId: String) {
        gameDao.finishGame(gameId)
    }

    suspend fun getStatsForPlayer(playerId: String): SchwimmenStats? =
        statsDao.getStatsForPlayer(playerId)

    fun getAllStats(): Flow<List<SchwimmenStats>> =
        statsDao.getAllStatsFlow()

    suspend fun ensureStatsExists(playerId: String) {
        val existing = statsDao.getStatsForPlayer(playerId)
        if (existing == null) {
            statsDao.insertStats(SchwimmenStats(playerId = playerId))
        }
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

    suspend fun recordRoundPlayed(playerId: String) {
        val stats = statsDao.getStatsForPlayer(playerId)
        if (stats != null) {
            statsDao.updateStats(
                stats.copy(roundsPlayedSchwimmen = stats.roundsPlayedSchwimmen + 1)
            )
        }
    }
}