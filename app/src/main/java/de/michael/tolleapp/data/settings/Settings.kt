package de.michael.tolleapp.data.settings

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Long = 0,
    val isDarkmode: Boolean = false,
)