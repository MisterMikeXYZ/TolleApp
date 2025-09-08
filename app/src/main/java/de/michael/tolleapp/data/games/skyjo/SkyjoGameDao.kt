package de.michael.tolleapp.data.games.skyjo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import de.michael.tolleapp.data.player.Player

@Dao
interface SkyjoGameDao {

    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: SkyjoGame) // Unit return type

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player) // Unit return type
    @Update
    suspend fun updateGame(game: SkyjoGame)

    @Delete
    suspend fun deleteGame(game: SkyjoGame)

    @Query("SELECT * FROM skyjo_games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: String): SkyjoGame?

    @Query("SELECT * FROM skyjo_games ORDER BY createdAt DESC")
    suspend fun getAllGames(): List<SkyjoGame>

    @Query("SELECT * FROM skyjo_games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SkyjoGame>>

    @Query("UPDATE skyjo_games SET endedAt = :endedAt WHERE id = :gameId")
    suspend fun markEnded(gameId: String, endedAt: Long)

    @Query("UPDATE skyjo_games SET dealerId = :dealerId WHERE id = :gameId")
    suspend fun setDealer(gameId: String, dealerId: String)

    @Query("SELECT dealerId FROM skyjo_games WHERE id = :gameId LIMIT 1")
    suspend fun getDealer(gameId: String): String?

    @Insert
    suspend fun insertWinners(winners: List<SkyjoGameWinner>)

    @Insert
    suspend fun insertLosers(losers: List<SkyjoGameLoser>)

    @Transaction
    suspend fun insertWinnersAndLosers(winners: List<SkyjoGameWinner>, losers: List<SkyjoGameLoser>) {
        if (winners.isNotEmpty()) insertWinners(winners)
        if (losers.isNotEmpty()) insertLosers(losers)
    }

    @Query("SELECT * FROM skyjo_game_winners WHERE gameId = :gameId")
    suspend fun getWinnersForGame(gameId: String): List<SkyjoGameWinner>

    @Query("SELECT * FROM skyjo_game_losers WHERE gameId = :gameId")
    suspend fun getLosersForGame(gameId: String): List<SkyjoGameLoser>

    @Delete
    suspend fun deleteWinner(winner: SkyjoGameWinner)

    @Delete
    suspend fun deleteLoser(loser: SkyjoGameLoser)
}

@Dao
interface SkyjoGameRoundDao {
    @Insert
    suspend fun insertRound(round: SkyjoGameRound)

    @Update
    suspend fun updateRound(round: SkyjoGameRound)

    @Query("DELETE FROM skyjo_game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)

    @Query("SELECT * FROM skyjo_game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC, id ASC")
    suspend fun getRoundsForGame(gameId: String): List<SkyjoGameRound>

    @Query("SELECT * FROM skyjo_game_rounds WHERE gameId = :gameId AND playerId = :playerId ORDER BY roundIndex ASC")
    suspend fun getRoundsForPlayerInGame(gameId: String, playerId: String): List<SkyjoGameRound>

    @Query("DELETE FROM skyjo_game_rounds WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun deleteRoundsForPlayerInGame(gameId: String, playerId: String)
}

@Dao
interface SkyjoGameStatisticsDao {
    @Query("""
        SELECT COUNT(*) FROM skyjo_games 
        WHERE id IN (SELECT gameId FROM skyjo_game_rounds WHERE playerId = :playerId)
    """)
    suspend fun getTotalGamesPlayed(playerId: String): Int

    @Query("""SELECT COUNT(*) FROM skyjo_game_winners WHERE playerId = :playerId""")
    suspend fun getGamesWon(playerId: String): Int

    @Query("""SELECT COUNT(*) FROM skyjo_game_losers  WHERE playerId = :playerId """)
    suspend fun getGamesLost(playerId: String): Int

    @Query("SELECT COUNT(*) FROM skyjo_game_rounds WHERE playerId = :playerId")
    suspend fun getRoundsPlayed(playerId: String): Int

    @Query("SELECT MAX(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
    suspend fun getBestRoundScore(playerId: String): Int?

    @Query("SELECT MIN(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
    suspend fun getWorstRoundScore(playerId: String): Int?

    @Query("SELECT AVG(roundScore) FROM skyjo_game_rounds WHERE playerId = :playerId")
    suspend fun getAverageRoundScore(playerId: String): Double?

    @Query("""
        SELECT MIN(totalScore) FROM (
            SELECT gameId, SUM(roundScore) AS totalScore
            FROM skyjo_game_rounds
            WHERE playerId = :playerId
            GROUP BY gameId
        )
    """)
    suspend fun getBestEndScore(playerId: String): Int?

    @Query("""
        SELECT MAX(totalScore) FROM (
            SELECT gameId, SUM(roundScore) AS totalScore
            FROM skyjo_game_rounds
            WHERE playerId = :playerId
            GROUP BY gameId
        )
    """)
    suspend fun getWorstEndScore(playerId: String): Int?

    @Query("""
        SELECT SUM(totalScore) FROM (
            SELECT gameId, SUM(roundScore) AS totalScore
            FROM skyjo_game_rounds
            WHERE playerId = :playerId
            GROUP BY gameId
        )
    """)
    suspend fun getTotalEndScore(playerId: String): Int?
}
