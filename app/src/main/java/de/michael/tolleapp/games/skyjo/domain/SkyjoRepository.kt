package de.michael.tolleapp.games.skyjo.domain

import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.statistics.gameStats.SkyjoStats
import kotlinx.coroutines.flow.Flow

interface SkyjoRepository {


    // Game operations ---------------------------------------------------------------------------
    suspend fun createGame(gameId: String): Result<Unit>
    suspend fun getGameById(gameId: String): Result<SkyjoGame?>
    fun getPausedGames(): Flow<List<PausedGame>>
    suspend fun finishGame(gameId: String): Result<Unit>
    suspend fun unfinishGame(gameId: String): Result<Unit>
    suspend fun deleteGame(gameId: String): Result<Unit>


    // Round operations --------------------------------------------------------------------------
    suspend fun upsertRound(gameId: String, roundData: SkyjoRoundData): Result<Unit>
    suspend fun upsertRound(gameId: String, roundData: List<SkyjoRoundData>): Result<Unit>
    suspend fun removeLastRound(gameId: String): Result<Unit>


    // Player operations -------------------------------------------------------------------------
    suspend fun addPlayerToGame(gameId: String, playerId: String, index: Int): Result<Unit>
    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>
    suspend fun setWinnerAndLoser(gameId: String, winners: List<String>, losers: List<String>): Result<Unit>
    suspend fun clearWinnersAndLosers(gameId: String, playerIds: List<String>): Result<Unit>
    suspend fun setDealer(gameId: String, dealerId: String): Result<Unit>

    // Statistics --------------------------------------------------------------------------------
    suspend fun getStatsForPlayer(playerId: String): SkyjoStats
}