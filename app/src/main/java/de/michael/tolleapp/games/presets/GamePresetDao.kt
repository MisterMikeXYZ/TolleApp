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

    @Query("SELECT * FROM game_presets WHERE gameType = :gameType AND name = :name LIMIT 1")
    suspend fun getPresetByNameAndGameType(gameType: String, name: String): GamePreset?

    @Transaction
    suspend fun insertPresetWithPlayers(gameType: String, preset: GamePreset, playerIds: List<String>) {
        val existing = getPresetByNameAndGameType(gameType, preset.name)
        if (existing != null) return
        val presetId = insertPreset(preset)
        val playerEntities = playerIds.map { pid ->
            GamePresetPlayer(presetId = presetId, playerId = pid)
        }
        insertPresetPlayers(playerEntities)
    }

    @Query("DELETE FROM game_presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: Long)

    @Transaction
    @Query("SELECT * FROM game_presets WHERE name = :name")
    suspend fun getPresetsByName(name: String): List<GamePresetWithPlayers>

    @Query("DELETE FROM game_preset_players WHERE presetId = :presetId")
    suspend fun deletePresetPlayersByPresetId(presetId: Long)


    @Transaction
    suspend fun deleteTestPresets() {
        val testPresetNames = listOf("Test", "Test Extrem", "Viebegs")
        val presetsToDelete = testPresetNames.flatMap { name ->
            getPresetsByName(name)
        }
        presetsToDelete.forEach { presetWithPlayers ->
            deletePresetPlayersByPresetId(presetWithPlayers.preset.id)
            deletePresetById(presetWithPlayers.preset.id)
        }
    }
}
