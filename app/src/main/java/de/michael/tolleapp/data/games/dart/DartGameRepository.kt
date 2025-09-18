import de.michael.tolleapp.data.games.dart.DartGame
import de.michael.tolleapp.data.games.dart.DartGameDao
import de.michael.tolleapp.data.games.dart.DartGameRound
import de.michael.tolleapp.data.player.Player
import kotlinx.coroutines.flow.Flow

class DartGameRepository(private val dao: DartGameDao) {

    // --- Players ---
    suspend fun addPlayer(name: String) {
        dao.insertPlayer(Player(name = name))
    }

    fun getAllPlayers(): Flow<List<Player>> = dao.getAllPlayers()

    // Games
    suspend fun addGame(gameStyle: Int, winnerId: String? = null): String {
        val game = DartGame(
            id = java.util.UUID.randomUUID().toString(),
            winnerId = winnerId,
            gameStyle = gameStyle)
        dao.insertGame(game)
        return game.id
    }

    suspend fun getAllGames(): List<DartGame> {
        return dao.getAllGames()
    }

    fun getActiveGames(): Flow<List<DartGame>> {
        return dao.getPausedGames()
    }

    suspend fun getGameById(gameId: String): DartGame? {
        return dao.getGameById(gameId)
    }

    suspend fun updateGame(game: DartGame) {
        dao.updateGame(game)
    }

//    suspend fun loadGame(gameId: String): Pair<Map<String, List<List<Int>>>, Int> {
//        val allRounds = dao.getRoundsForGame(gameId)
//        val gameStyle = dao.getGameById(gameId)?.gameStyle ?: 301
//
//        val roundsByPlayer = allRounds.groupBy { it.playerId }
//            .mapValues { entry ->
//                entry.value.sortedBy { it.roundIndex }
//                    .map { listOf(it.dart1, it.dart2, it.dart3) }
//            }
//
//        return roundsByPlayer to gameStyle
//    }

    suspend fun deleteGame(game: DartGame) {
        dao.deleteGame(game)
    }

    suspend fun markGameFinished(gameId: String, winnerId: String) {
        val game = dao.getGameById(gameId) ?: return
        dao.updateGame(game.copy(isFinished = true, winnerId = winnerId))
    }


    // Rounds
//    suspend fun addRound(
//        gameId: String,
//        playerId: String,
//        roundIndex: Int,
//        dart1: Int,
//        dart2: Int,
//        dart3: Int
//    ) {
//        val round = DartGameRound(
//            gameId = gameId,
//            playerId = playerId,
//            roundIndex = roundIndex,
//            dart1 = dart1,
//            dart2 = dart2,
//            dart3 = dart3
//        )
//        dao.insertRound(round)
//    }

    suspend fun getRoundsForGame(gameId: String): List<DartGameRound> {
        return dao.getRoundsForGame(gameId)
    }

    suspend fun getRoundsForPlayerInGame(gameId: String, playerId: String): List<DartGameRound> {
        return dao.getRoundsForPlayerInGame(gameId, playerId)
    }

    suspend fun updateRound(round: DartGameRound) {
        dao.updateRound(round)
    }

    suspend fun deleteRound(round: DartGameRound) {
        dao.deleteRound(round)
    }

    // --- Statistics ---
    suspend fun getTotalGamesPlayed(playerId: String): Int {
        return dao.getTotalGamesPlayed(playerId)
    }

    suspend fun getGamesWon(playerId: String): Int {
        return dao.getGamesWon(playerId)
    }

    suspend fun getGamesLost(playerId: String): Int {
        return dao.getGamesLost(playerId)
    }

    suspend fun getRoundsPlayed(playerId: String): Int {
        return dao.getRoundsPlayed(playerId)
    }

//    suspend fun getBestRoundScore(playerId: String): Int {
//        return dao.getBestRoundScore(playerId) ?: 0
//    }
//
//    suspend fun getWorstRoundScore(playerId: String): Int {
//        return dao.getWorstRoundScore(playerId) ?: 0
//    }
//
//    suspend fun getAverageRoundScore(playerId: String): Double {
//        return dao.getAverageRoundScore(playerId) ?: 0.0
//    }
//
//    suspend fun getAverageFirstDart(playerId: String): Double {
//        return dao.getAverageFirstDart(playerId) ?: 0.0
//    }
//
//    suspend fun getAverageSecondDart(playerId: String): Double {
//        return dao.getAverageSecondDart(playerId) ?: 0.0
//    }
//
//    suspend fun getAverageThirdDart(playerId: String): Double {
//        return dao.getAverageThirdDart(playerId) ?: 0.0
//    }
//
//    suspend fun getPerfectRounds(playerId: String): Int {
//        return dao.getPerfectRounds(playerId)
//    }
//
//    suspend fun getTripleTwentyHits(playerId: String): Int {
//        return dao.getTripleTwentyHits(playerId)
//    }
}
