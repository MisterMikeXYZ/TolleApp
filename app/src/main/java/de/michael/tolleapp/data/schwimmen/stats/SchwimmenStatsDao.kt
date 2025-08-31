package de.michael.tolleapp.data.schwimmen.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SchwimmenStatsDao {
    @Query("SELECT * FROM schwimmen_stats WHERE playerId = :playerId LIMIT 1")
    suspend fun getStatsForPlayer(playerId: String): SchwimmenStats?

    @Insert
    suspend fun insertStats(stats: SchwimmenStats)

    @Query("SELECT * FROM schwimmen_stats")
    fun getAllStatsFlow(): Flow<List<SchwimmenStats>>

    @Query("SELECT * FROM schwimmen_stats WHERE playerId IN (:playerIds)")
    suspend fun getStatsForPlayers(playerIds: List<String>): List<SchwimmenStats>

    @Update
    suspend fun updateStats(stats: SchwimmenStats)

    @Query("SELECT * FROM schwimmen_stats")
    fun getAllStats(): Flow<List<SchwimmenStats>>
}