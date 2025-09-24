package de.michael.tolleapp.games.wizard.domain

import de.michael.tolleapp.games.wizard.data.WizardDao
import de.michael.tolleapp.games.wizard.data.WizardRepository
import de.michael.tolleapp.games.wizard.data.entities.WizardGameEntity
import de.michael.tolleapp.games.wizard.data.entities.WizardRoundEntity
import de.michael.tolleapp.games.wizard.data.mappers.toDomain
import de.michael.tolleapp.games.wizard.data.mappers.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WizardRepositoryImpl(
    private val dao: WizardDao
): WizardRepository {
    override fun createGame(gameId: String, roundsToPlay: Int): Result<Unit> {
        try {
            dao.createGame(gameId, roundsToPlay)
        } catch (e: Exception) {
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

    override fun finishGame(gameId: String): Result<Unit> {
        try {
            dao.finishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override fun deleteGame(gameId: String): Result<Unit> {
        try {
            dao.deleteGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override fun upsertRound(
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

    override fun addPlayerToGame(
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

    override fun removePlayerFromGame(
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