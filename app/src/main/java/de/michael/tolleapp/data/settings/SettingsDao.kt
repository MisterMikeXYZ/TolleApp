package de.michael.tolleapp.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SettingsDao {

    @Query("SELECT isDarkmode FROM settings WHERE id = 0 LIMIT 1")
    suspend fun getDarkmode(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)
}