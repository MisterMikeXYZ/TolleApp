package de.michael.tolleapp.data.skyjo.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SkyjoStatsDao {
    @Query("SELECT * FROM skyjo_stats WHERE playerId = :playerId LIMIT 1")
    suspend fun getStatsForPlayer(playerId: String): SkyjoStats?

    @Insert
    suspend fun insertStats(stats: SkyjoStats)

    @Query("SELECT * FROM skyjo_stats")
    fun getAllStatsFlow(): Flow<List<SkyjoStats>>

    @Query("SELECT * FROM skyjo_stats WHERE playerId IN (:playerIds)")
    suspend fun getStatsForPlayers(playerIds: List<String>): List<SkyjoStats>

    @Update
    suspend fun updateStats(stats: SkyjoStats)

    @Query("SELECT * FROM skyjo_stats")
    fun getAllStats(): Flow<List<SkyjoStats>>
}

@Dao
interface SkyjoRoundResultDao {
    @Insert
    suspend fun insertRoundResult(result: SkyjoRoundResult)

    @Query("SELECT * FROM skyjo_round_results WHERE gameId = :gameId AND playerId = :playerId")
    suspend fun getRoundsForPlayer(gameId: String, playerId: String): List<SkyjoRoundResult>
}
