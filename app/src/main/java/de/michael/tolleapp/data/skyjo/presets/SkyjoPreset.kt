package de.michael.tolleapp.data.skyjo.presets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "skyjo_presets")
data class SkyjoPreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skyjo_preset_players",
    foreignKeys = [
        ForeignKey(
            entity = SkyjoPreset::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presetId"), Index("playerId")]
)
data class SkyjoPresetPlayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val playerId: String
)
