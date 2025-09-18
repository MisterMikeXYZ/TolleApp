package de.michael.tolleapp.data.games.dart

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DartThrowDao {
    // Games
    @Insert
    suspend fun insertGame(game: DartGame)

    @Query("SELECT * FROM dart_games")
    suspend fun getAllGames(): List<DartGame>

    @Transaction
    @Query("SELECT * FROM dart_games WHERE id = :gameId")
    suspend fun getGameWithRounds(gameId: String): GameWithRounds?

    // Rounds
    @Insert
    suspend fun insertRound(round: DartGameRound): Long
    @Transaction
    @Query("SELECT * FROM dart_game_rounds WHERE gameId = :gameId")
    suspend fun getRoundsForGame(gameId: String): List<RoundWithThrows>

    // Throws
    @Insert
    suspend fun insertThrow(throwData: DartThrowData)
    @Query("SELECT * FROM dart_throws WHERE roundId = :roundId ORDER BY throwIndex ASC")
    suspend fun getThrowsForRound(roundId: Long): List<DartThrowData>
}

