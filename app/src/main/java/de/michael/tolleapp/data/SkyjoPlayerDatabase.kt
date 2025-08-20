package de.michael.tolleapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<SkyjoPlayer>>

    @Insert
    suspend fun insertPlayer(player: SkyjoPlayer)

    @Update
    suspend fun updatePlayer(player: SkyjoPlayer)

    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): SkyjoPlayer?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: String): SkyjoPlayer?
}

@Dao
interface RoundResultDao {
    @Insert
    suspend fun insertRoundResult(result: RoundResult)

    @Query("SELECT * FROM round_results WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun getRoundsForPlayer(gameId: String, playerId: String): List<RoundResult>
}
