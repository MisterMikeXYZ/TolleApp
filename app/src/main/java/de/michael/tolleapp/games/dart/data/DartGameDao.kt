package de.michael.tolleapp.games.dart.data

import androidx.room.*
import de.michael.tolleapp.games.player.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface DartGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    // Game handling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: DartGame)

    @Update
    suspend fun updateGame(game: DartGame)

    @Query("DELETE FROM dart_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    @Query("DELETE FROM dart_game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)

    @Query("SELECT * FROM dart_games WHERE id = :gameId LIMIT 1")
    suspend fun getGameById(gameId: String): DartGame?

    @Query("SELECT * FROM dart_games ORDER BY createdAt DESC")
    suspend fun getAllGames(): List<DartGame>

    @Query("SELECT * FROM dart_games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<DartGame>>

    @Query("SELECT * FROM dart_games WHERE winnerId = :playerId")
    suspend fun getGamesWonByPlayer(playerId: String): List<DartGame>


    // Round handling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: DartGameRound) : Long

    @Update
    suspend fun updateRound(round: DartGameRound)

    @Delete
    suspend fun deleteRound(round: DartGameRound)

    @Query("SELECT * FROM dart_game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    suspend fun getRoundsForGame(gameId: String): List<DartGameRound>

    @Query("SELECT * FROM dart_game_rounds WHERE gameId = :gameId AND playerId = :playerId ORDER BY roundIndex ASC")
    suspend fun getRoundsForPlayerInGame(gameId: String, playerId: String): List<DartGameRound>


    // Statistics queries

    @Query("""
        SELECT COUNT(*) FROM dart_games 
        WHERE id IN (SELECT gameId FROM dart_game_rounds WHERE playerId = :playerId)
    """)
    suspend fun getTotalGamesPlayed(playerId: String): Int

    @Query("SELECT COUNT(*) FROM dart_games WHERE winnerId = :playerId")
    suspend fun getGamesWon(playerId: String): Int

    @Query("""
        SELECT (COUNT(*) - (
            SELECT COUNT(*) FROM dart_games WHERE winnerId = :playerId
        )) FROM dart_games 
        WHERE id IN (SELECT gameId FROM dart_game_rounds WHERE playerId = :playerId)
        """)
    suspend fun getGamesLost(playerId: String): Int

    @Query("SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId")
    suspend fun getRoundsPlayed(playerId: String): Int

//    @Query("SELECT MAX(dart1 + dart2 + dart3) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getBestRoundScore(playerId: String): Int?
//
//    @Query("SELECT MIN(dart1 + dart2 + dart3) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getWorstRoundScore(playerId: String): Int?
//
//    @Query("SELECT AVG(dart1 + dart2 + dart3) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getAverageRoundScore(playerId: String): Double?
//
//    @Query("SELECT AVG(dart1) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getAverageFirstDart(playerId: String): Double?
//
//    @Query("SELECT AVG(dart2) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getAverageSecondDart(playerId: String): Double?
//
//    @Query("SELECT AVG(dart3) FROM dart_game_rounds WHERE playerId = :playerId")
//    suspend fun getAverageThirdDart(playerId: String): Double?
//
//    @Query("""
//        SELECT COUNT(*) FROM dart_game_rounds
//        WHERE playerId = :playerId AND dart1 = 60 AND dart2 = 60 AND dart3 = 60
//        """)
//    suspend fun getPerfectRounds(playerId: String): Int
//
//    @Query("""
//        SELECT (
//            (SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId AND dart1 = 60) +
//            (SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId AND dart2 = 60) +
//            (SELECT COUNT(*) FROM dart_game_rounds WHERE playerId = :playerId AND dart3 = 60)
//        )
//        """)
//    suspend fun getTripleTwentyHits(playerId: String): Int
}
