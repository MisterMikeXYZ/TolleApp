package de.michael.tolleapp.games.wizard.data

import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.games.wizard.domain.WizardGame
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import kotlinx.coroutines.flow.Flow

interface WizardRepository {

    // Game operations ----------------------------------------------------------------------------
    suspend fun createGame(gameId: String, roundsToPlay: Int): Result<Unit>

    fun getGame(gameId: String): Result<Flow<WizardGame?>>

    fun getPausedGames(): Result<Flow<List<PausedGame>>>

    suspend fun finishGame(gameId: String): Result<Unit>

    suspend fun deleteGame(gameId: String): Result<Unit>

    // Round operations ---------------------------------------------------------------------------
    suspend fun upsertRound(gameId: String, roundData: WizardRoundData): Result<Unit>
    suspend fun upsertRound(gameId: String, roundData: List<WizardRoundData>): Result<Unit>

    // Player operations --------------------------------------------------------------------------
    suspend fun addPlayerToGame(gameId: String, playerId: String): Result<Unit>

    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>
}