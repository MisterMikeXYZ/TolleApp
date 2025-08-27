package de.michael.tolleapp.data.skyjo.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SkyjoGameRepository(
    private val gameDao: GameDao,
    private val gameRoundDao: GameRoundDao,
) {
    suspend fun startGame(gameId: String) {
        gameDao.insertGame(SkyjoGame(id = gameId))
    }

    suspend fun continueGame(gameId: String) {
        val existing = gameDao.getGame(gameId)
        if (existing == null) {
            throw IllegalStateException("Cannot continue non-existing game with id $gameId")
        }
        if (existing.isFinished) {
            throw IllegalStateException("Cannot continue finished game with id $gameId")
        }
        gameDao.updateGame(SkyjoGame(id = gameId))
    }

    suspend fun ensureSession(gameId: String) {
        val existing = gameDao.getGame(gameId)
        if (existing == null) gameDao.insertGame(SkyjoGame(id = gameId))
    }

    suspend fun addRound(gameId: String, playerId: String, roundIndex: Int, score: Int) {
        gameRoundDao.insertRound(
            GameRound(
                gameId = gameId,
                playerId = playerId,
                roundIndex = roundIndex,
                roundScore = score
            )
        )
    }

    suspend fun getRoundsGroupedByPlayer(gameId: String): Map<String, List<Int>> {
        val rounds = gameRoundDao.getRoundsForGame(gameId)
        return rounds.groupBy { it.playerId }.mapValues { (_, list) -> list.map { it.roundScore } }
    }

    suspend fun markEnded(gameId: String) {
        gameDao.markEnded(gameId, System.currentTimeMillis())
    }

    suspend fun deleteGameCompletely(gameId: String) {
        gameRoundDao.deleteRoundsForGame(gameId)
        gameDao.deleteGame(gameId)
    }

    fun getPausedGames(): Flow<List<SkyjoGame>> = gameDao.getPausedGames()

    suspend fun saveSnapshot(gameId: String, perPlayerRounds: Map<String, List<Int>>) {
        withContext(Dispatchers.IO) {
            ensureSession(gameId)
            gameRoundDao.deleteRoundsForGame(gameId)
            // re-insert in stable roundIndex order
            // longest list defines number of rounds
            val maxRounds = perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
            for (roundIdx in 1..maxRounds) {
                perPlayerRounds.forEach { (playerId, scores) ->
                    val score = scores.getOrNull(roundIdx - 1) ?: return@forEach
                    gameRoundDao.insertRound(
                        GameRound(
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

    suspend fun loadGame(gameId: String): List<GameRound> =
        gameRoundDao.getRoundsForGame(gameId)
}