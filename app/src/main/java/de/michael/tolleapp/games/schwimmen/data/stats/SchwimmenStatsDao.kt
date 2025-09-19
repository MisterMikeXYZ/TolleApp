package de.michael.tolleapp.games.schwimmen.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: SchwimmenStats)

    @Query("SELECT * FROM schwimmen_stats")
    fun getAllStats(): Flow<List<SchwimmenStats>>
}