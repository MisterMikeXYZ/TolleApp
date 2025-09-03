package de.michael.tolleapp.data.schwimmen.game

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SchwimmenGameRepository (
    private val gameDao: SchwimmenGameDao,
    private val roundDao: SchwimmenGameRoundDao,
) {
    suspend fun startGame(gameId: String, screenType: GameScreenType) {
        gameDao.insertGame(SchwimmenGame(id = gameId, screenType = screenType))
    }

    suspend fun continueGame(gameId: String) {
        val existing = gameDao.getGame(gameId)
        if (existing == null) {
            throw IllegalStateException("Cannot continue non-existing game")
        }
        if (existing.isFinished) {
            throw IllegalStateException("Cannot continue finished game")
        }
        val screenType = existing.screenType
        gameDao.updateGame(SchwimmenGame(id = gameId, screenType = screenType))
    }

    suspend fun addRound(gameId: String, playerId: String, roundIndex: Int, playerLives: Int) {
        roundDao.insertRound(
            SchwimmenGameRound(
                gameId = gameId,
                playerId = playerId,
                roundIndex = roundIndex,
                lives = playerLives,
            )
        )
    }

    suspend fun getRoundsGroupedByPlayer(gameId: String): Map<String, Int> {
        val rounds = roundDao.getRoundsForGame(gameId)
        return rounds.groupBy { it.playerId }
            .mapValues { entry -> entry.value.maxByOrNull { it.roundIndex }?.lives ?: 4 }
    }
    suspend fun markEnded(gameId: String) {
        gameDao.markEnded(gameId, System.currentTimeMillis())
    }
    suspend fun deleteGameCompletely(gameId: String) {
        roundDao.deleteRoundsForGame(gameId)
        gameDao.deleteGame(gameId)
    }
    fun getPausedGames(): Flow<List<SchwimmenGame>> = gameDao.getPausedGames()
    suspend fun saveSnapshot(gameId: String, perPlayerRounds: Map<String, Int>) {
        withContext(Dispatchers.IO) {
            roundDao.deleteRoundsForGame(gameId)
            perPlayerRounds.forEach { (playerId, lives) ->
                roundDao.insertRound(
                    SchwimmenGameRound(
                        gameId = gameId,
                        playerId = playerId,
                        roundIndex = 1, // only storing current lives
                        lives = lives
                    )
                )
            }
        }
    }
    suspend fun loadGame(gameId: String) : List<SchwimmenGameRound> = roundDao.getRoundsForGame(gameId)
}