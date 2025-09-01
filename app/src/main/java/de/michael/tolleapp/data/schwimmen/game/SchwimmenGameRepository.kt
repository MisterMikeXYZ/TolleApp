package de.michael.tolleapp.data.schwimmen.game

import de.michael.tolleapp.data.schwimmen.stats.SchwimmenGamePlayer
import kotlinx.coroutines.flow.Flow

class SchwimmenGameRepository (
    private val gameDao: SchwimmenGameDao,
    private val playerDao: SchwimmenGamePlayerDao,
    private val roundPlayerDao: RoundPlayerDao,
) {
    suspend fun saveGameSnapshot(
        gameId: String,
        playerIds: List<String>,
        playerLives: Map<String, Int>,
        dealerIndex: Int
    ) {
        // Insert the round first
        val round = GameRound(gameId = gameId, dealerIndex = dealerIndex)
        val roundId = gameDao.insertRound(round)  // <-- needs to return Long

        // Insert round players
        val roundPlayers = playerIds.map { id ->
            RoundPlayer(roundId = roundId, playerId = id, lives = playerLives[id] ?: 0)
        }
        roundPlayerDao.insertRoundPlayers(roundPlayers)
    }
    suspend fun createGame(gameId: String, screenType: GameScreenType): SchwimmenGame {
        val game = SchwimmenGame(
            id = gameId,
            createdAt = System.currentTimeMillis(),
            screenType = screenType
        )
        gameDao.insertGame(game)
        return game
    }


    suspend fun getGameById(gameId: String): SchwimmenGame? =
        gameDao.getGameById(gameId)

    fun getAllGames(): Flow<List<SchwimmenGame>> =
        gameDao.getAllGames()

    suspend fun finishGame(gameId: String) {
        gameDao.finishGame(gameId)
    }

    suspend fun deleteGameCompletely(gameId: String) {
        gameDao.deleteGame(gameId)
    }

    // Players
    suspend fun addPlayersToGame(players: List<SchwimmenGamePlayer>) =
        playerDao.insertPlayers(players)

    suspend fun getPlayersForRound(roundId: Long): List<RoundPlayer> =
        roundPlayerDao.getPlayersForRound(roundId)

    suspend fun getPlayersForGame(gameId: String): List<SchwimmenGamePlayer> =
        playerDao.getPlayersForGame(gameId)

    suspend fun getAlivePlayers(gameId: String): List<SchwimmenGamePlayer> =
        playerDao.getAlivePlayers(gameId)

    suspend fun updatePlayer(player: SchwimmenGamePlayer) = playerDao.updatePlayer(player)

    suspend fun getPlayer(gameId: String, playerId: String): SchwimmenGamePlayer? =
        playerDao.getPlayer(gameId, playerId)


    suspend fun getLatestRound(gameId: String): GameRound? = gameDao.getLatestRound(gameId)

    suspend fun getAllRoundsForGame(gameId: String): List<GameRound> = gameDao.getAllRoundsForGame(gameId)
}