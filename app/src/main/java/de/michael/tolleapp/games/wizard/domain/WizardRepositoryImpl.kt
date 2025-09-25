package de.michael.tolleapp.games.wizard.domain

import android.util.Log
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.wizard.data.WizardDao
import de.michael.tolleapp.games.wizard.data.WizardRepository
import de.michael.tolleapp.games.wizard.data.entities.WizardGameEntity
import de.michael.tolleapp.games.wizard.data.entities.WizardRoundEntity
import de.michael.tolleapp.games.wizard.data.mappers.toDomain
import de.michael.tolleapp.games.wizard.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class WizardRepositoryImpl(
    private val dao: WizardDao
): WizardRepository {
    companion object {
        private const val TAG = "WizardRepositoryImpl"
    }

    override suspend fun createGame(gameId: String, roundsToPlay: Int): Result<Unit> {
        try {
            dao.upsertGame(WizardGameEntity(
                id = gameId,
                roundsToPlay = roundsToPlay
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create game", e)
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override fun getGame(gameId: String): Result<Flow<WizardGame?>> {
        val gameFlow: Flow<WizardGameEntity?>
        val playersFlow: Flow<List<String>>
        val roundsFlow: Flow<List<WizardRoundEntity>>
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
        roundData: WizardRoundData
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
        roundData: List<WizardRoundData>
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