package de.michael.tolleapp.settings.data.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Long = 0,
    val isDarkmode: Boolean = false,
)