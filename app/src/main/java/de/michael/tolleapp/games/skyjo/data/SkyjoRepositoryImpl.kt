package de.michael.tolleapp.games.skyjo.data

import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoRoundEntity
import de.michael.tolleapp.games.skyjo.data.mappers.toDomain
import de.michael.tolleapp.games.skyjo.data.mappers.toEntity
import de.michael.tolleapp.games.skyjo.domain.SkyjoGame
import de.michael.tolleapp.games.skyjo.domain.SkyjoRepository
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class SkyjoRepositoryImpl(
    private val skyjoDao: SkyjoDao,
    //private val skyjoStatisticsDao: SkyjoGameStatisticsDao,
): SkyjoRepository {


    // Game operations ----------------------------------------------------------------------------
    override suspend fun createGame(gameId: String, endPoints: Int): Result<Unit> {
        try {
            skyjoDao.upsertGame(SkyjoGameEntity(id = gameId, endPoints = endPoints))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override fun getGameById(gameId: String): Result<Flow<SkyjoGame?>> {
        val gameFlow: Flow<SkyjoGameEntity?>
        val playersFlow: Flow<List<String>>
        val roundsFlow: Flow<List<SkyjoRoundEntity>>
        try {
            gameFlow = skyjoDao.getGameById(gameId)
            playersFlow = skyjoDao.getPlayerIdsForGame(gameId)
            roundsFlow = skyjoDao.getRoundsForGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        combine(
            gameFlow,
            playersFlow,
            roundsFlow
        ) { gameEntity, players, roundEntities ->
            gameEntity?.toDomain(
                playerIds = players,
                rounds = roundEntities.map { it.toDomain() },

                )
        }.let { combinedFlow ->
            return Result.success(combinedFlow)
        }
    }

    override fun getPausedGames(): Result<Flow<List<PausedGame>>> {
        try {
            skyjoDao.getPausedGames().map {
                it.map { entity ->
                    PausedGame(
                        id = entity.id,
                        createdAt = entity.createdAt
                    )
                }
            }.let { flow ->
                return Result.success(flow)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun finishGame(gameId: String): Result<Unit> {
        try {
            skyjoDao.finishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun unfinishGame(gameId: String): Result<Unit> {
        try {
            skyjoDao.unfinishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteGame(gameId: String): Result<Unit> {
        try {
            skyjoDao.deleteGameById(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }


    // Round operations ---------------------------------------------------------------------------
    override suspend fun upsertRound(gameId: String, roundData: SkyjoRoundData): Result<Unit> {
        try {
            skyjoDao.upsertRound(roundData.toEntity(gameId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun upsertRound(
        gameId: String,
        roundData: List<SkyjoRoundData>
    ): Result<Unit> {
        try {
            roundData.forEach { roundData ->
                skyjoDao.upsertRound(roundData.toEntity(gameId))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun removeLastRound(gameId: String): Result<Unit> {
        try {
            skyjoDao.removeLastRound(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }


    // Player operations --------------------------------------------------------------------------
    override suspend fun addPlayerToGame(gameId: String, playerId: String): Result<Unit> {
        try {
            skyjoDao.addPlayerToGame(gameId, playerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit> {
        try {
            skyjoDao.removePlayerFromGame(gameId, playerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun setWinnerAndLoser(gameId: String, winners: List<String>, losers: List<String>): Result<Unit> {
        try {
            winners.forEach { skyjoDao.setPlayerAsWinner(gameId, playerId = it) }
            losers.forEach { skyjoDao.setPlayerAsLoser(gameId, playerId = it) }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun clearWinnersAndLosers(gameId: String, playerIds: List<String>): Result<Unit> {
        try {
            skyjoDao.clearWinnersAndLosers(gameId, playerIds)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun setDealer(gameId: String, dealerId: String): Result<Unit> {
        try {
            val game = skyjoDao.getGameById(gameId).first()
                ?: return Result.failure(IllegalStateException("Game with id $gameId does not exist"))
            skyjoDao.upsertGame(game.copy(dealerId = dealerId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }






//    suspend fun continueGame(gameId: String) {
//        val existing = skyjoDao.getGameById(gameId)
//        if (existing == null) {
//            throw IllegalStateException("Cannot continue non-existing game with id $gameId")
//        }
//        if (existing.isFinished) {
//            throw IllegalStateException("Cannot continue finished game with id $gameId")
//        }
//        skyjoDao.updateGame(existing.copy(isFinished = false))
//        //skyjoGameDao.updateGame(SkyjoGame(id = gameId))
//    }
//
//    suspend fun removeLastRound(gameId: String, playerId: String) {
//        val rounds = skyjoGameRoundDao.getRoundsForPlayerInGame(gameId, playerId)
//        if (rounds.isEmpty()) return
//        val lastRound = rounds.maxByOrNull { it.roundIndex } ?: return
//        skyjoGameRoundDao.deleteRoundById(lastRound.id)
//    }
//
//
//    suspend fun loadGame(gameId: String): List<SkyjoRoundEntity> =
//        skyjoGameRoundDao.getRoundsForGame(gameId)
//

    // Statistics operations ----------------------------------------------------------------------
//    suspend fun resetAllGameStats() {
//        withContext(Dispatchers.IO) {
//            // 1️⃣ Delete all rounds
//            val allGames = skyjoDao.getAllGames()
//            allGames.forEach { game ->
//                skyjoGameRoundDao.deleteRoundsForGame(game.id)
//            }
//
//            // 2️⃣ Delete all winners and losers
//            allGames.forEach { game ->
//                val winners = skyjoDao.getWinnersForGame(game.id)
//                val losers = skyjoDao.getLosersForGame(game.id)
//
//                winners.forEach { skyjoDao.deleteWinner(it) }
//                //losers.forEach { skyjoDao.deleteLoser(it) }
//            }
//
//            // 3️⃣ Delete all games
//            allGames.forEach { game ->
//                skyjoDao.deleteGame(game)
//            }
//        }
//    }
//
//    suspend fun getTotalGamesPlayed(playerId: String): Int {
//        return skyjoStatisticsDao.getTotalGamesPlayed(playerId)
//    }
//
//    suspend fun getGamesWon(playerId: String): Int {
//        return skyjoStatisticsDao.getGamesWon(playerId)
//    }
//
//    suspend fun getGamesLost(playerId: String): Int {
//        return skyjoStatisticsDao.getGamesLost(playerId)
//    }
//
//    suspend fun getRoundsPlayed(playerId: String): Int {
//        return skyjoStatisticsDao.getRoundsPlayed(playerId)
//    }
//
//    suspend fun getBestRoundScore(playerId: String): Int? {
//        return skyjoStatisticsDao.getBestRoundScore(playerId)
//    }
//
//    suspend fun getWorstRoundScore(playerId: String): Int? {
//        return skyjoStatisticsDao.getWorstRoundScore(playerId)
//    }
//
//    suspend fun getAverageRoundScore(playerId: String): Double? {
//        return skyjoStatisticsDao.getAverageRoundScore(playerId)
//    }
//
//    suspend fun getBestEndScore(playerId: String): Int? {
//        return skyjoStatisticsDao.getBestEndScore(playerId)
//    }
//
//    suspend fun getWorstEndScore(playerId: String): Int? {
//        return skyjoStatisticsDao.getWorstEndScore(playerId)
//    }
//
//    suspend fun getTotalEndScore(playerId: String): Int? {
//        return skyjoStatisticsDao.getTotalEndScore(playerId)
//    }
}