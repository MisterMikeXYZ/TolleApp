import de.michael.tolleapp.games.dart.data.DartGame
import de.michael.tolleapp.games.dart.data.DartGameDao
import de.michael.tolleapp.games.dart.data.DartGameRound
import de.michael.tolleapp.games.dart.data.DartThrowDao
import de.michael.tolleapp.games.dart.data.DartThrowData
import de.michael.tolleapp.games.util.player.Player
import kotlinx.coroutines.flow.Flow

class DartGameRepository(
    private val gameDao: DartGameDao,
    private val throwDao: DartThrowDao
) {
    suspend fun addPlayer(player: Player): Boolean {
        return try {
            gameDao.insertPlayer(player)
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Games ---
    suspend fun insertGame(game: DartGame) = gameDao.insertGame(game)

    suspend fun updateGame(game: DartGame) = gameDao.updateGame(game)

    suspend fun getGameById(id: String) = gameDao.getGameById(id)

    suspend fun deleteGameCompletely(gameId: String) {
        gameDao.deleteRoundsForGame(gameId)
        gameDao.deleteGame(gameId)
    }

    suspend fun getAllGames() = gameDao.getAllGames()

    fun getPausedGames() = gameDao.getPausedGames()

    fun getActiveGames(): Flow<List<DartGame>> {
        return gameDao.getPausedGames()
    }
    // --- Rounds ---

    suspend fun insertRound(round: DartGameRound): Long = gameDao.insertRound(round)

    suspend fun updateRound(round: DartGameRound) = gameDao.updateRound(round)

    suspend fun deleteRound(round: DartGameRound) = gameDao.deleteRound(round)

    suspend fun getRoundsForGame(gameId: String) = gameDao.getRoundsForGame(gameId)

    suspend fun getRoundsForPlayerInGame(gameId: String, playerId: String) =
        gameDao.getRoundsForPlayerInGame(gameId, playerId)

    // --- Throws ---

    suspend fun insertThrow(throwData: DartThrowData) = throwDao.insertThrow(throwData)

    suspend fun getThrowsForRound(roundId: Long) = throwDao.getThrowsForRound(roundId)

    // --- Statistics ---

    suspend fun getTotalGamesPlayed(playerId: String) = gameDao.getTotalGamesPlayed(playerId)

    suspend fun getGamesWon(playerId: String) = gameDao.getGamesWon(playerId)

    suspend fun getGamesLost(playerId: String) = gameDao.getGamesLost(playerId)

    suspend fun getRoundsPlayed(playerId: String) = gameDao.getRoundsPlayed(playerId)

    // --- Transactional ---

    suspend fun getGameWithRounds(gameId: String) = throwDao.getGameWithRounds(gameId)
}
