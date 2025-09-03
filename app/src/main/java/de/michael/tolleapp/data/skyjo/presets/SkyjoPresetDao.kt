package de.michael.tolleapp.data.skyjo.presets

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class SkyjoPresetWithPlayers(
    @Embedded val preset: SkyjoPreset,
    @Relation(
        parentColumn = "id",
        entityColumn = "presetId",
        entity = SkyjoPresetPlayer::class
    )
    val players: List<SkyjoPresetPlayer>
)

@Dao
interface SkyjoPresetDao {

    @Transaction
    @Query("SELECT * FROM skyjo_presets")
    fun getPresets(): Flow<List<SkyjoPresetWithPlayers>>

    @Transaction
    @Query("SELECT * FROM skyjo_presets WHERE id = :id")
    fun getPreset(id: Long): Flow<SkyjoPresetWithPlayers>

    @Insert
    suspend fun insertPreset(preset: SkyjoPreset): Long

    @Insert
    suspend fun insertPresetPlayers(players: List<SkyjoPresetPlayer>)

    @Transaction
    suspend fun insertPresetWithPlayers(preset: SkyjoPreset, playerIds: List<String>) {
        val presetId = insertPreset(preset)
        val playerEntities = playerIds.map { pid ->
            SkyjoPresetPlayer(presetId = presetId, playerId = pid)
        }
        insertPresetPlayers(playerEntities)
    }

    @Query("DELETE FROM skyjo_presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: Long)
}
