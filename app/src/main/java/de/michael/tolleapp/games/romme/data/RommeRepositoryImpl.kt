package de.michael.tolleapp.games.romme.data

import de.michael.tolleapp.games.romme.data.entities.RommeGameEntity
import de.michael.tolleapp.games.romme.data.entities.RommeRoundEntity
import de.michael.tolleapp.games.romme.data.mappers.toDomain
import de.michael.tolleapp.games.romme.data.mappers.toEntity
import de.michael.tolleapp.games.romme.domain.RommeGame
import de.michael.tolleapp.games.romme.domain.RommeRoundData
import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RommeRepositoryImpl(
    val dao: RommeDao
): RommeRepository {
    override suspend fun createGame(gameId: String): Result<Unit> {
        return try {
            dao.upsertGame(RommeGameEntity(id = gameId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGame(gameId: String): Result<Flow<RommeGame?>> {
        val gameFlow: Flow<RommeGameEntity?>
        val playersFlow: Flow<List<String>>
        val roundsFlow: Flow<List<RommeRoundEntity>>
        try {
            gameFlow = dao.getGame(gameId)
            playersFlow = dao.getPlayerIdsForGame(gameId)
            roundsFlow = dao.getRoundsForGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        combine(
            gameFlow,
            playersFlow,
            roundsFlow
        ) { gameEntity, playerIds, roundEntities ->
            gameEntity?.toDomain(
                playerIds = playerIds,
                rounds = roundEntities.map { it.toDomain() }
            )
        }.let { combinedFlow ->
            return Result.success(combinedFlow)
        }
    }

    override fun getPausedGames(): Result<Flow<List<PausedGame>>> {
        try {
            dao.getPausedGames().map {
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
            dao.finishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteGame(gameId: String): Result<Unit> {
        try {
            dao.deleteGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun upsertRound(
        gameId: String,
        roundData: RommeRoundData
    ): Result<Unit> {
        try {
            dao.upsertRound(roundData.toEntity(gameId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun upsertRound(
        gameId: String,
        roundData: List<RommeRoundData>
    ): Result<Unit> {
        try {
            roundData.forEach { roundData ->
                dao.upsertRound(roundData.toEntity(gameId))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun addPlayerToGame(
        gameId: String,
        playerId: String
    ): Result<Unit> {
        try {
            dao.addPlayerToGame(gameId, playerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun removePlayerFromGame(
        gameId: String,
        playerId: String
    ): Result<Unit> {
        try {
            dao.removePlayerFromGame(gameId, playerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
}