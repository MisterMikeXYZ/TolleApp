package de.michael.tolleapp.games.romme.domain

import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.flow.Flow

interface RommeRepository {

    // Game operations ----------------------------------------------------------------------------
    suspend fun createGame(gameId: String): Result<Unit>

    fun getGame(gameId: String): Result<Flow<RommeGame?>>

    fun getPausedGames(): Result<Flow<List<PausedGame>>>

    suspend fun finishGame(gameId: String): Result<Unit>

    suspend fun deleteGame(gameId: String): Result<Unit>

    // Round operations ---------------------------------------------------------------------------
    suspend fun upsertRound(gameId: String, roundData: RommeRoundData): Result<Unit>
    suspend fun upsertRound(gameId: String, roundData: List<RommeRoundData>): Result<Unit>

    // Player operations --------------------------------------------------------------------------
    suspend fun addPlayerToGame(gameId: String, playerId: String): Result<Unit>

    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>
}