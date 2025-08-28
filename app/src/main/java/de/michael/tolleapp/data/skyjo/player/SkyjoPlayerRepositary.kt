package de.michael.tolleapp.data.skyjo.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlayerRepository(
    private val playerDao: PlayerDao,
) {

    fun getPlayers(): Flow<List<SkyjoPlayer>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: SkyjoPlayer): Boolean {
        val existing = playerDao.getPlayerByName(player.name)
        if (existing != null) return false
        playerDao.insertPlayer(player)
        return true
    }

    private suspend fun getPlayersOnce(): List<SkyjoPlayer> {
        return playerDao.getAllPlayers()
            .first() // requires kotlinx.coroutines.flow.first()
    }

    suspend fun updateRoundStats(playerId: String, roundScore: Int) {
        val player = playerDao.getPlayerById(playerId) ?: return

        val bestRound = if (player.bestRoundScoreSkyjo == null || roundScore < player.bestRoundScoreSkyjo) roundScore else player.bestRoundScoreSkyjo
        val worstRound = if (player.worstRoundScoreSkyjo == null || roundScore > player.worstRoundScoreSkyjo) roundScore else player.worstRoundScoreSkyjo

        val updated = player.copy(
            bestRoundScoreSkyjo = bestRound,
            worstRoundScoreSkyjo = worstRound,
            roundsPlayedSkyjo = player.roundsPlayedSkyjo + 1,
            totalEndScoreSkyjo = player.totalEndScoreSkyjo + roundScore
        )
        playerDao.updatePlayer(updated)
    }

    suspend fun updateEndStats(playerId: String, endScore: Int) {
        val player = playerDao.getPlayerById(playerId) ?: return

        val bestEnd = if (player.bestEndScoreSkyjo == null || endScore < player.bestEndScoreSkyjo) endScore else player.bestEndScoreSkyjo
        val worstEnd = if (player.worstEndScoreSkyjo == null || endScore > player.worstEndScoreSkyjo) endScore else player.worstEndScoreSkyjo

        val updated = player.copy(
            bestEndScoreSkyjo = bestEnd,
            worstEndScoreSkyjo = worstEnd,
            totalGamesPlayedSkyjo = player.totalGamesPlayedSkyjo + 1,
        )
        playerDao.updatePlayer(updated)
    }

    suspend fun incrementWonGames(playerId: String) {
        val player = playerDao.getPlayerById(playerId) ?: return
        val updated = player.copy(
            wonGames = player.wonGames + 1
        )
        playerDao.updatePlayer(updated)
    }

    suspend fun incrementLostGames(playerId: String) {
        val player = playerDao.getPlayerById(playerId) ?: return
        val updated = player.copy(
            lostGames = player.lostGames + 1
        )
        playerDao.updatePlayer(updated)
    }

    suspend fun resetAllGameStats()
    {
        val players = getPlayersOnce()
        players.forEach { player ->
            val resetPlayer = player.copy(
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
            playerDao.updatePlayer(resetPlayer)
        }
    }
}

