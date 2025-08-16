package de.michael.tolleapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<Player>>

    @Insert
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): Player?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: String): Player?
}

@Dao
interface RoundResultDao {
    @Insert
    suspend fun insertRoundResult(result: RoundResult)

    @Query("SELECT * FROM round_results WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun getRoundsForPlayer(gameId: String, playerId: String): List<RoundResult>
}
