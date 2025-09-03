package de.michael.tolleapp.data.skyjo.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SkyjoGameRepository(
    private val skyjoGameDao: SkyjoGameDao,
    private val skyjoGameRoundDao: SkyjoGameRoundDao,
) {
    suspend fun startGame(gameId: String) {
        skyjoGameDao.insertGame(SkyjoGame(id = gameId))
    }

    suspend fun continueGame(gameId: String) {
        val existing = skyjoGameDao.getGame(gameId)
        if (existing == null) {
            throw IllegalStateException("Cannot continue non-existing game with id $gameId")
        }
        if (existing.isFinished) {
            throw IllegalStateException("Cannot continue finished game with id $gameId")
        }
        skyjoGameDao.updateGame(SkyjoGame(id = gameId))
    }

    suspend fun ensureSession(gameId: String) {
        val existing = skyjoGameDao.getGame(gameId)
        if (existing == null) skyjoGameDao.insertGame(SkyjoGame(id = gameId))
    }

    suspend fun addRound(gameId: String, playerId: String, roundIndex: Int, score: Int) {
        skyjoGameRoundDao.insertRound(
            SkyjoGameRound(
                gameId = gameId,
                playerId = playerId,
                roundIndex = roundIndex,
                roundScore = score
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
        skyjoGameDao.deleteGame(gameId)
    }

    fun getPausedGames(): Flow<List<SkyjoGame>> = skyjoGameDao.getPausedGames()

    suspend fun saveSnapshot(gameId: String, perPlayerRounds: Map<String, List<Int>>) {
        withContext(Dispatchers.IO) {
            ensureSession(gameId)
            skyjoGameRoundDao.deleteRoundsForGame(gameId)
            // re-insert in stable roundIndex order
            // longest list defines number of rounds
            val maxRounds = perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
            for (roundIdx in 1..maxRounds) {
                perPlayerRounds.forEach { (playerId, scores) ->
                    val score = scores.getOrNull(roundIdx - 1) ?: return@forEach
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
}