package de.michael.tolleapp.games.skyjo.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoRoundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkyjoDao {

    // Game operations ----------------------------------------------------------------------------
    @Upsert
    suspend fun upsertGame(gameEntity: SkyjoGameEntity)
    @Query("SELECT * FROM SkyjoGameEntity WHERE id = :gameId LIMIT 1")
    fun getGameById(gameId: String): Flow<SkyjoGameEntity?>
    @Query("SELECT * FROM SkyjoGameEntity WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SkyjoGameEntity>>
    @Query("UPDATE SkyjoGameEntity SET isFinished = 1 WHERE id = :gameId")
    suspend fun finishGame(gameId: String)
    @Query("UPDATE SkyjoGameEntity SET isFinished = 0 WHERE id = :gameId")
    suspend fun unfinishGame(gameId: String)
    @Query("DELETE FROM SkyjoGameEntity WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String)


    // Round operations ---------------------------------------------------------------------------
    @Query("SELECT * FROM SkyjoRoundEntity WHERE gameId = :gameId ORDER BY roundNumber ASC")
    fun getRoundsForGame(gameId: String): Flow<List<SkyjoRoundEntity>>
    @Upsert
    suspend fun upsertRound(round: SkyjoRoundEntity)
    @Query("DELETE FROM SkyjoRoundEntity WHERE gameId = :gameId ORDER BY roundNumber DESC LIMIT 1")
    suspend fun removeLastRound(gameId: String)


    // Player operations --------------------------------------------------------------------------
    @Query("SELECT playerId FROM WizardGamePlayerEntity WHERE gameId = :gameId")
    fun getPlayerIdsForGame(gameId: String): Flow<List<String>>
    @Query("INSERT INTO SkyjoPlayerEntity (gameId, playerId) VALUES (:gameId, :playerId)")
    suspend fun addPlayerToGame(gameId: String, playerId: String)
    @Query("UPDATE SkyjoPlayerEntity SET isWinner = 1 WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setPlayerAsWinner(gameId: String, playerId: String)
    @Query("UPDATE SkyjoPlayerEntity SET isLoser = 1 WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun setPlayerAsLoser(gameId: String, playerId: String)
    @Query("UPDATE SkyjoPlayerEntity SET isWinner = 0, isLoser = 0 WHERE gameId = :gameId AND playerId IN (:playerIds)")
    suspend fun clearWinnersAndLosers(gameId: String, playerIds: List<String>)
    @Query("DELETE FROM SkyjoPlayerEntity WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun removePlayerFromGame(gameId: String, playerId: String)
}











//@Dao
//interface SkyjoGameStatisticsDao {
//    @Query("""
//        SELECT COUNT(*) FROM SkyjoGameEntity
//        WHERE id IN (SELECT id FROM SkyjoGameEntity WHERE playerId = :playerId)
//    """)
//    suspend fun getTotalGamesPlayed(playerId: String): Int
//
//    @Query("""SELECT COUNT(*) FROM skyjo_game_winners WHERE playerId = :playerId""")
//    suspend fun getGamesWon(playerId: String): Int
//
//    @Query("""SELECT COUNT(*) FROM skyjo_game_losers  WHERE playerId = :playerId """)
//    suspend fun getGamesLost(playerId: String): Int
//
//    @Query("SELECT COUNT(*) FROM skyjo_game_rounds WHERE playerId = :playerId")
//    suspend fun getRoundsPlayed(playerId: String): Int
//
//    @Query("SELECT MIN(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
//    suspend fun getBestRoundScore(playerId: String): Int?
//
//    @Query("SELECT MAX(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
//    suspend fun getWorstRoundScore(playerId: String): Int?
//
//    @Query("SELECT AVG(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
//    suspend fun getAverageRoundScore(playerId: String): Double?
//
//    @Query("""
//        SELECT MIN(totalScore) FROM (
//            SELECT gameId, SUM(roundScore) AS totalScore
//            FROM skyjo_game_rounds
//            WHERE playerId = :playerId
//            GROUP BY gameId
//        )
//    """)
//    suspend fun getBestEndScore(playerId: String): Int?
//
//    @Query("""
//        SELECT MAX(totalScore) FROM (
//            SELECT gameId, SUM(roundScore) AS totalScore
//            FROM skyjo_game_rounds
//            WHERE playerId = :playerId
//            GROUP BY gameId
//        )
//    """)
//    suspend fun getWorstEndScore(playerId: String): Int?
//
//    @Query("""
//        SELECT SUM(totalScore) FROM (
//            SELECT gameId, SUM(roundScore) AS totalScore
//            FROM skyjo_game_rounds
//            WHERE playerId = :playerId
//            GROUP BY gameId
//        )
//    """)
//    suspend fun getTotalEndScore(playerId: String): Int?
//}
