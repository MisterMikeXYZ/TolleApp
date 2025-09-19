package de.michael.tolleapp.games.dart.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DartThrowDao {
    @Transaction
    @Query("SELECT * FROM dart_games WHERE id = :gameId")
    suspend fun getGameWithRounds(gameId: String): GameWithRounds?
    @Insert
    suspend fun insertThrow(throwData: DartThrowData)
    @Query("SELECT * FROM dart_throws WHERE roundId = :roundId ORDER BY throwIndex ASC")
    suspend fun getThrowsForRound(roundId: Long): List<DartThrowData>
}

