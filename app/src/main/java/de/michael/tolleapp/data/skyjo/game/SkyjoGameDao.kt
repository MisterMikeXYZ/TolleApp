package de.michael.tolleapp.data.skyjo.game

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert
    suspend fun insertGame(game: SkyjoGame)

    @Update
    suspend fun updateGame(game: SkyjoGame)

    @Query("UPDATE games SET endedAt = :endedAt WHERE id = :gameId")
    suspend fun markEnded(gameId: String, endedAt: Long)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGame(id: String): SkyjoGame?

    @Query("SELECT * FROM games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SkyjoGame>>
}

@Dao
interface GameRoundDao {
    @Insert
    suspend fun insertRound(round: GameRound)

    @Query("SELECT * FROM game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC, id ASC")
    suspend fun getRoundsForGame(gameId: String): List<GameRound>

    @Query("DELETE FROM game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)
}
