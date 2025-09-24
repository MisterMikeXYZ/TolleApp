package de.michael.tolleapp.settings.data

class SettingsRepository(
    private val settingsDao: SettingsDao
) {
    suspend fun changeDarkmode(newValue: Boolean) {
        settingsDao.insertSettings(Settings(id = 0, isDarkmode = newValue))
    }

    suspend fun isDarkmode(): Boolean {
        return settingsDao.getDarkmode()
    }
}