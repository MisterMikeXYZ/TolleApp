package de.michael.tolleapp.games.wizard.data

import de.michael.tolleapp.games.wizard.domain.WizardGame
import de.michael.tolleapp.games.wizard.domain.WizardRoundData
import kotlinx.coroutines.flow.Flow

interface WizardRepository {

    // Game operations ----------------------------------------------------------------------------
    fun createGame(gameId: String, roundsToPlay: Int): Result<Unit>

    fun getGame(gameId: String): Result<Flow<WizardGame?>>

    fun finishGame(gameId: String): Result<Unit>

    fun deleteGame(gameId: String): Result<Unit>

    // Round operations ---------------------------------------------------------------------------
    fun upsertRound(gameId: String, roundData: WizardRoundData): Result<Unit>

    // Player operations --------------------------------------------------------------------------
    fun addPlayerToGame(gameId: String, playerId: String): Result<Unit>

    fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>
}