package de.michael.tolleapp.games.flip7.data

import de.michael.tolleapp.games.flip7.data.entities.Flip7GameEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7PlayerEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7RoundEntity
import de.michael.tolleapp.games.flip7.data.mappers.toDomain
import de.michael.tolleapp.games.flip7.data.mappers.toEntity
import de.michael.tolleapp.games.flip7.domain.Flip7Game
import de.michael.tolleapp.games.flip7.domain.Flip7Repository
import de.michael.tolleapp.games.flip7.domain.Flip7RoundData
import de.michael.tolleapp.games.util.PausedGame
import de.michael.tolleapp.statistics.gameStats.Flip7Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class Flip7RepositoryImpl(
    private val flip7Dao: Flip7Dao,
    private val flip7StatsDao: Flip7StatisticsDao,
): Flip7Repository {
    
    // Game operations ---------------------------------------------------------------------------
    override suspend fun createGame(gameId: String): Result<Unit> {
        try {
            flip7Dao.upsertGame(Flip7GameEntity(id = gameId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteGame(gameId: String): Result<Unit> {
        try {
            flip7Dao.deleteGameById(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun finishGame(gameId: String): Result<Unit> {
        try {
            flip7Dao.finishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun unfinishGame(gameId: String): Result<Unit> {
        try {
            flip7Dao.unfinishGame(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun getGameById(gameId: String): Result<Flip7Game?> {
        val game: Flip7GameEntity?
        val players: List<String>
        val rounds: List<Flip7RoundEntity>
        try {
            game = flip7Dao.getGameById(gameId)
            players = flip7Dao.getPlayerIdsForGame(gameId)
            rounds = flip7Dao.getRoundsForGame(gameId)
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

    override fun getPausedGames(): Flow<List<PausedGame>> {
        return flip7Dao.getPausedGames()
            .map { entities ->
                entities.map { e -> PausedGame(id = e.id, createdAt = e.createdAt) }
        }
    }
    
    
    
    // Round operations --------------------------------------------------------------------------
    override suspend fun removeLastRound(gameId: String): Result<Unit> {
        try {
            flip7Dao.removeLastRound(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }


    override suspend fun upsertRound(gameId: String, roundData: Flip7RoundData): Result<Unit> {
        try {
            flip7Dao.upsertRound(roundData.toEntity(gameId))
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    
    // Player operations -------------------------------------------------------------------------
    override suspend fun addPlayerToGame(gameId: String, playerId: String, index: Int): Result<Unit> {
        try {
            flip7Dao.upsertPlayerInGame(
                Flip7PlayerEntity(
                    gameId = gameId,
                    playerId = playerId,
                    index = index,
                )
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    override suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit> {
        try {
            flip7Dao.removePlayerFromGame(gameId, playerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    override suspend fun setDealer(gameId: String, dealerId: String): Result<Unit> {
        try {
            val game = flip7Dao.getGameById(gameId)
                ?: return Result.failure(IllegalStateException("Game with id $gameId does not exist"))
            flip7Dao.setDealer(game.id, dealerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    override suspend fun setWinner(gameId: String, winnerId: String): Result<Unit> {
        try {
            flip7Dao.setPlayerAsWinner(gameId, winnerId = winnerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }
    
    override suspend fun clearWinner(gameId: String): Result<Unit> {
        try {
            flip7Dao.clearWinner(gameId)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return Result.success(Unit)
    }

    override suspend fun getStatsForPlayer(playerId: String): Flip7Stats = withContext(Dispatchers.IO) {
        val totalGames = flip7StatsDao.getTotalGamesPlayed(playerId)
        val gamesWon = flip7StatsDao.getGamesWon(playerId)
        val roundsPlayed = flip7StatsDao.getRoundsPlayed(playerId)

        // Fetch all round entities for this player
        val roundEntities = flip7StatsDao.getRoundsForPlayer(playerId)

        // Convert entities to domain models (via your mapper)
        val roundData = roundEntities.map { it.toDomain() }

        // Collect all per-round scores for the player
        val playerRoundScores = roundData.mapNotNull { it.scores[playerId] }

        // Calculate round-level stats
        val bestRound = playerRoundScores.maxOrNull()
        val worstRound = playerRoundScores.minOrNull()
        val avgRound = playerRoundScores.takeIf { it.isNotEmpty() }?.average()

        // Compute total scores per game (end scores)
        val totalEndByGame = roundData
            .groupBy { it.roundNumber } // each roundData belongs to one game
            .mapValues { (_, roundsForGame) ->
                roundsForGame.sumOf { round -> round.scores[playerId] ?: 0 }
            }

        val bestEnd = totalEndByGame.values.maxOrNull()
        val worstEnd = totalEndByGame.values.minOrNull()
        val totalEnd = totalEndByGame.values.sum().takeIf { it != 0 }

        Flip7Stats(
            playerId = playerId,
            totalGames = totalGames,
            gamesWon = gamesWon,
            roundsPlayed = roundsPlayed,
            bestRound = bestRound,
            worstRound = worstRound,
            avgRound = avgRound,
            bestEnd = bestEnd,
            worstEnd = worstEnd,
            totalEnd = totalEnd
        )
    }

    override suspend fun resetAllGameStats() {
        flip7StatsDao.deleteAllFinishedGames()
    }
}
