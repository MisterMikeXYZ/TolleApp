package de.michael.tolleapp.games.player

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM players")
    suspend fun getAllPlayersOnce(): List<Player>

    @Delete
    suspend fun deletePlayer(player: Player)

    @Transaction
    suspend fun deletePlayerAndPresets(player: Player) {
        val presetIds = getPresetIdsByPlayer(player.id)
        deletePlayer(player)
        deletePresetsByIds(presetIds)
    }

    @Query("DELETE FROM game_presets WHERE id IN (:presetIds)")
    suspend fun deletePresetsByIds(presetIds: List<Long>)

    @Query("SELECT presetId FROM game_preset_players WHERE playerId = :playerId")
    suspend fun getPresetIdsByPlayer(playerId: String): List<Long>

    @Query("SELECT * FROM players WHERE name = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): Player?

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: String): Player?
}
