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

    suspend fun finalizePlayerStats(
        playerId: String,
        rounds: List<Int>,
        isWinner: Boolean,
        isLoser: Boolean
    ) {
        val player = playerDao.getPlayerById(playerId) ?: return

        val endScore = rounds.sum()

        // Round stats
        val bestRound = listOfNotNull(player.bestRoundScoreSkyjo, rounds.minOrNull()).minOrNull()
        val worstRound = listOfNotNull(player.worstRoundScoreSkyjo, rounds.maxOrNull()).maxOrNull()

        // End-of-game stats
        val bestEnd = listOfNotNull(player.bestEndScoreSkyjo, endScore).minOrNull()
        val worstEnd = listOfNotNull(player.worstEndScoreSkyjo, endScore).maxOrNull()

        val updated = player.copy(
            bestRoundScoreSkyjo = bestRound,
            worstRoundScoreSkyjo = worstRound,
            bestEndScoreSkyjo = bestEnd,
            worstEndScoreSkyjo = worstEnd,
            roundsPlayedSkyjo = player.roundsPlayedSkyjo + rounds.size,
            totalEndScoreSkyjo = player.totalEndScoreSkyjo + endScore,
            totalGamesPlayedSkyjo = player.totalGamesPlayedSkyjo + 1,
            wonGames = if (isWinner) player.wonGames + 1 else player.wonGames,
            lostGames = if (isLoser) player.lostGames + 1 else player.lostGames,
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

