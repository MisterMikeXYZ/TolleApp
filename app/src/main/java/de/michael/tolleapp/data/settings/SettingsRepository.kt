package de.michael.tolleapp.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDao: SettingsDao
) {
    suspend fun changeDarkmode(newValue: Boolean) {
        settingsDao.insertSettings(Settings(id = 0, isDarkmode = newValue))
    }

    suspend fun isDarkmode(): Boolean {
        return settingsDao.getDarkmode() ?: false
    }
}