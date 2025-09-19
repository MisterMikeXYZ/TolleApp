package de.michael.tolleapp.games.skyjo.data

import de.michael.tolleapp.games.player.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SkyjoGameRepository(
    private val skyjoGameDao: SkyjoGameDao,
    private val skyjoGameRoundDao: SkyjoGameRoundDao,
    private val skyjoStatisticsDao: SkyjoGameStatisticsDao,
) {
    suspend fun startGame(gameId: String, dealerId: String? = null) {
        skyjoGameDao.insertGame(SkyjoGame(id = gameId, dealerId = dealerId))
    }

    suspend fun addPlayer(player: Player): Boolean {
        return try {
            skyjoGameDao.insertPlayer(player)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllPlayers(): Flow<List<Player>> = skyjoGameDao.getAllPlayers()

    suspend fun continueGame(gameId: String) {
        val existing = skyjoGameDao.getGameById(gameId)
        if (existing == null) {
            throw IllegalStateException("Cannot continue non-existing game with id $gameId")
        }
        if (existing.isFinished) {
            throw IllegalStateException("Cannot continue finished game with id $gameId")
        }
        skyjoGameDao.updateGame(existing.copy(isFinished = false))
        //skyjoGameDao.updateGame(SkyjoGame(id = gameId))
    }

    suspend fun ensureSession(gameId: String) {
        val existing = skyjoGameDao.getGameById(gameId)
        if (existing == null) skyjoGameDao.insertGame(SkyjoGame(id = gameId))
    }

    suspend fun addRound(gameId: String, playerId: String, roundIndex: Int, score: Int) {
        skyjoGameRoundDao.insertRound(
            SkyjoGameRound(
                gameId = gameId,
                playerId = playerId,
                roundIndex = roundIndex,
                roundScore = score,
            )
        )
    }

    suspend fun getRoundsGroupedByPlayer(gameId: String): Map<String, List<Int>> {
        val rounds = skyjoGameRoundDao.getRoundsForGame(gameId)
        return rounds.groupBy { it.playerId }.mapValues { (_, list) -> list.map { it.roundScore } }
    }

    suspend fun markEnded(gameId: String) {
        skyjoGameDao.markEnded(gameId, System.currentTimeMillis())
    }

    suspend fun deleteGameCompletely(gameId: String) {
        skyjoGameRoundDao.deleteRoundsForGame(gameId)
        val game = skyjoGameDao.getGameById(gameId)
        if (game != null) {
            skyjoGameDao.deleteGame(game)
        }
    }

    fun getPausedGames(): Flow<List<SkyjoGame>> = skyjoGameDao.getPausedGames()

    suspend fun saveSnapshot(gameId: String, perPlayerRounds: Map<String, List<Int>>) {
        withContext(Dispatchers.IO) {
            ensureSession(gameId)
            skyjoGameRoundDao.deleteRoundsForGame(gameId)
            val maxRounds = perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
            for (roundIdx in 0 until maxRounds) {
                perPlayerRounds.forEach { (playerId, scores) ->
                    val score = scores.getOrNull(roundIdx) ?: return@forEach
                    skyjoGameRoundDao.insertRound(
                        SkyjoGameRound(
                            gameId = gameId,
                            playerId = playerId,
                            roundIndex = roundIdx,
                            roundScore = score
                        )
                    )
                }
            }
        }
    }

    suspend fun loadGame(gameId: String): List<SkyjoGameRound> =
        skyjoGameRoundDao.getRoundsForGame(gameId)

    suspend fun setDealer(gameId: String, dealerId: String?) {
        val game = skyjoGameDao.getGameById(gameId) ?: return
        skyjoGameDao.updateGame(game.copy(dealerId = dealerId))
    }

    suspend fun getDealer(gameId: String): String? {
        return skyjoGameDao.getGameById(gameId)?.dealerId
    }

    suspend fun insertWinnersAndLosers(
        gameId: String,
        winners: List<SkyjoGameWinner>,
        losers: List<SkyjoGameLoser>
    ) {
        // Ensure game exists (optional)
        skyjoGameDao.insertWinnersAndLosers(
            winners.map { it.copy(gameId = gameId) },
            losers.map { it.copy(gameId = gameId) }
        )
    }

    suspend fun resetAllGameStats() {
        withContext(Dispatchers.IO) {
            // 1️⃣ Delete all rounds
            val allGames = skyjoGameDao.getAllGames()
            allGames.forEach { game ->
                skyjoGameRoundDao.deleteRoundsForGame(game.id)
            }

            // 2️⃣ Delete all winners and losers
            allGames.forEach { game ->
                val winners = skyjoGameDao.getWinnersForGame(game.id)
                val losers = skyjoGameDao.getLosersForGame(game.id)

                winners.forEach { skyjoGameDao.deleteWinner(it) }
                losers.forEach { skyjoGameDao.deleteLoser(it) }
            }

            // 3️⃣ Delete all games
            allGames.forEach { game ->
                skyjoGameDao.deleteGame(game)
            }
        }
    }

    suspend fun getTotalGamesPlayed(playerId: String): Int {
        return skyjoStatisticsDao.getTotalGamesPlayed(playerId)
    }

    suspend fun getGamesWon(playerId: String): Int {
        return skyjoStatisticsDao.getGamesWon(playerId)
    }

    suspend fun getGamesLost(playerId: String): Int {
        return skyjoStatisticsDao.getGamesLost(playerId)
    }

    suspend fun getRoundsPlayed(playerId: String): Int {
        return skyjoStatisticsDao.getRoundsPlayed(playerId)
    }

    suspend fun getBestRoundScore(playerId: String): Int? {
        return skyjoStatisticsDao.getBestRoundScore(playerId)
    }

    suspend fun getWorstRoundScore(playerId: String): Int? {
        return skyjoStatisticsDao.getWorstRoundScore(playerId)
    }

    suspend fun getAverageRoundScore(playerId: String): Double? {
        return skyjoStatisticsDao.getAverageRoundScore(playerId)
    }

    suspend fun getBestEndScore(playerId: String): Int? {
        return skyjoStatisticsDao.getBestEndScore(playerId)
    }

    suspend fun getWorstEndScore(playerId: String): Int? {
        return skyjoStatisticsDao.getWorstEndScore(playerId)
    }

    suspend fun getTotalEndScore(playerId: String): Int? {
        return skyjoStatisticsDao.getTotalEndScore(playerId)
    }
}