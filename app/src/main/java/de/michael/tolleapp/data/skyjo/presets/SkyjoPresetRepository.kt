package de.michael.tolleapp.data.skyjo.presets

import kotlinx.coroutines.flow.Flow

class SkyjoPresetRepository(private val dao: SkyjoPresetDao) {

    fun getPresets(): Flow<List<SkyjoPresetWithPlayers>> = dao.getPresets()

    suspend fun createPreset(name: String, playerIds: List<String>) {
        dao.insertPresetWithPlayers(
            SkyjoPreset(name = name),
            playerIds
        )
    }

    suspend fun deletePreset(presetId: Long) {
        dao.deletePresetById(presetId)
    }
}
