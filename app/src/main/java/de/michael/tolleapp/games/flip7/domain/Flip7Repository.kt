package de.michael.tolleapp.games.flip7.domain

import de.michael.tolleapp.games.skyjo.domain.SkyjoGame
import de.michael.tolleapp.games.skyjo.domain.SkyjoRoundData
import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.flow.Flow

interface Flip7Repository {

    // Game operations ---------------------------------------------------------------------------
    suspend fun createGame(gameId: String): Result<Unit>
    suspend fun getGameById(gameId: String): Result<Flip7Game?>
    fun getPausedGames(): Flow<List<PausedGame>>
    suspend fun finishGame(gameId: String): Result<Unit>
    suspend fun unfinishGame(gameId: String): Result<Unit>
    suspend fun deleteGame(gameId: String): Result<Unit>

    // Round operations --------------------------------------------------------------------------
    suspend fun upsertRound(gameId: String, roundData: Flip7RoundData): Result<Unit>
    suspend fun removeLastRound(gameId: String): Result<Unit>

    // Player operations -------------------------------------------------------------------------
    suspend fun addPlayerToGame(gameId: String, playerId: String, index: Int): Result<Unit>
    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>
    suspend fun setWinner(gameId: String, winnerId: String): Result<Unit>
    suspend fun clearWinner(gameId: String): Result<Unit>
    suspend fun setDealer(gameId: String, dealerId: String): Result<Unit>
}