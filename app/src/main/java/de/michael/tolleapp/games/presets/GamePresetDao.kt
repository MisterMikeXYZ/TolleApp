package de.michael.tolleapp.games.presets

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class GamePresetWithPlayers(
    @Embedded val preset: GamePreset,
    @Relation(
        parentColumn = "id",
        entityColumn = "presetId",
        entity = GamePresetPlayer::class
    )
    val players: List<GamePresetPlayer>
)

@Dao
interface GamePresetDao {

    @Transaction
    @Query("SELECT * FROM game_presets WHERE gameType = :gameType")
    fun getPresets(gameType: String): Flow<List<GamePresetWithPlayers>>

    @Transaction
    @Query("SELECT * FROM game_presets WHERE id = :id")
    fun getPreset(id: Long): Flow<GamePresetWithPlayers>

    @Insert
    suspend fun insertPreset(preset: GamePreset): Long

    @Insert
    suspend fun insertPresetPlayers(players: List<GamePresetPlayer>)

    @Transaction
    suspend fun insertPresetWithPlayers(gameType: String, preset: GamePreset, playerIds: List<String>) {
        val presetId = insertPreset(preset)
        val playerEntities = playerIds.map { pid ->
            GamePresetPlayer(presetId = presetId, playerId = pid)
        }
        insertPresetPlayers(playerEntities)
    }

    @Query("DELETE FROM game_presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: Long)
}
