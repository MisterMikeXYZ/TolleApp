package de.michael.tolleapp.data.games.presets

import kotlinx.coroutines.flow.Flow

class GamePresetRepository(private val dao: GamePresetDao) {

    fun getPresets(gameType: String): Flow<List<GamePresetWithPlayers>> = dao.getPresets(gameType)

    suspend fun createPreset(gameType: String, name: String, playerIds: List<String>) {
        dao.insertPresetWithPlayers(
            gameType,
            GamePreset(name = name, gameType = gameType),
            playerIds
        )
    }

    suspend fun deletePreset(presetId: Long) {
        dao.deletePresetById(presetId)
    }
}
