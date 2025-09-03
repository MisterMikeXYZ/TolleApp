package de.michael.tolleapp.data.skyjo.game

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SkyjoGameDao {
    @Insert
    suspend fun insertGame(game: SkyjoGame)

    @Update
    suspend fun updateGame(game: SkyjoGame)

    @Query("UPDATE skyjo_games SET endedAt = :endedAt WHERE id = :gameId")
    suspend fun markEnded(gameId: String, endedAt: Long)

    @Query("DELETE FROM skyjo_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: String)

    @Query("SELECT * FROM skyjo_games WHERE id = :id LIMIT 1")
    suspend fun getGame(id: String): SkyjoGame?

    @Query("SELECT * FROM skyjo_games WHERE isFinished = 0 ORDER BY createdAt DESC")
    fun getPausedGames(): Flow<List<SkyjoGame>>
}

@Dao
interface SkyjoGameRoundDao {
    @Insert
    suspend fun insertRound(round: SkyjoGameRound)

    @Query("SELECT * FROM skyjo_game_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC, id ASC")
    suspend fun getRoundsForGame(gameId: String): List<SkyjoGameRound>

    @Query("DELETE FROM skyjo_game_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: String)
}
