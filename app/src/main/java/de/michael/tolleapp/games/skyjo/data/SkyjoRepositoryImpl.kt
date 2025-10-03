package de.michael.tolleapp.games.skyjo.data

import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoPlayerEntity
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
    override suspend fun createGame(gameId: String): Result<Unit> {
        try {
            skyjoDao.upsertGame(SkyjoGameEntity(id = gameId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun getGameById(gameId: String): Result<SkyjoGame?> {
        val game: SkyjoGameEntity?
        val players: List<String>
        val rounds: List<SkyjoRoundEntity>
        try {
            game = skyjoDao.getGameById(gameId)
            players = skyjoDao.getPlayerIdsForGame(gameId)
            rounds = skyjoDao.getRoundsForGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        game?.toDomain(
            playerIds = players,
            rounds = rounds.map { it.toDomain() },
        ).let { domainGame ->
            return Result.success(domainGame)
        }
    }

    override fun getPausedGames(): Flow<List<PausedGame>> =
        skyjoDao.getPausedGames()
            .map { entities ->
                entities.map { e -> PausedGame(id = e.id, createdAt = e.createdAt) }
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
    override suspend fun addPlayerToGame(gameId: String, playerId: String, index: Int): Result<Unit> {
        try {
            skyjoDao.upsertPlayerInGame(SkyjoPlayerEntity(
                gameId = gameId,
                playerId = playerId,
                index = index,
            ))
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
            val game = skyjoDao.getGameById(gameId)
                ?: return Result.failure(IllegalStateException("Game with id $gameId does not exist"))
            skyjoDao.setDealer(game.id, dealerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
}