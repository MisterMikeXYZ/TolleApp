package de.michael.tolleapp.data

import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val playerDao: PlayerDao,
    private val roundResultDao: RoundResultDao
) {

    fun getPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()

    suspend fun addPlayer(player: Player): Boolean {
        if (playerDao.getPlayerByName(player.name) != null) return false
        playerDao.insertPlayer(player)
        return true
    }

    suspend fun recordRound(gameId: String, playerId: String, roundScore: Int) {
        // insert round
        val round = RoundResult(playerId = playerId, gameId = gameId, roundScore = roundScore)
        roundResultDao.insertRoundResult(round)

        // recalc aggregates
        val rounds = roundResultDao.getRoundsForPlayer(gameId, playerId)
        val bestRound = rounds.minOfOrNull { it.roundScore } ?: roundScore
        val worstRound = rounds.maxOfOrNull { it.roundScore } ?: roundScore
        val totalRounds = rounds.size
        val totalRoundScore = rounds.sumOf { it.roundScore }

        // FIX: previously used getPlayerByName(playerId) which is incorrect; use getPlayerById
        val player = playerDao.getPlayerById(playerId) ?: return
        val updated = player.copy(
            bestRoundScoreSkyjo = minOf(player.bestRoundScoreSkyjo ?: Int.MAX_VALUE, bestRound),
            worstRoundScoreSkyjo = maxOf(player.worstRoundScoreSkyjo ?: Int.MIN_VALUE, worstRound),
            roundsPlayedSkyjo = totalRounds,
            totalRoundScoreSkyjo = player.totalRoundScoreSkyjo + roundScore
        )
        playerDao.updatePlayer(updated)
    }

    suspend fun endGame(gameId: String) {
        val players = playerDao.getAllPlayers() // fetch all players as Flow
        players.collect { playerList ->
            playerList.forEach { player ->
                val rounds = roundResultDao.getRoundsForPlayer(gameId, player.id)
                if (rounds.isEmpty()) return@forEach
                val endScore = rounds.sumOf { it.roundScore }
                val updated = player.copy(
                    bestEndScoreSkyjo = minOf(player.bestEndScoreSkyjo ?: Int.MAX_VALUE, endScore),
                    worstEndScoreSkyjo = maxOf(player.worstEndScoreSkyjo ?: Int.MIN_VALUE, endScore),
                    totalGamesPlayedSkyjo = player.totalGamesPlayedSkyjo + 1,
                    totalEndScoreSkyjo = player.totalEndScoreSkyjo + endScore
                )
                playerDao.updatePlayer(updated)
            }
        }
    }
}
