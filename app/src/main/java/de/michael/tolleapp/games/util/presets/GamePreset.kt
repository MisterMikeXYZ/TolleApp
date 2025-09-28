package de.michael.tolleapp.games.util.presets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.games.util.player.Player

@Entity(tableName = "game_presets")
data class GamePreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: GameType,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "game_preset_players",
    foreignKeys = [
        ForeignKey(
            entity = GamePreset::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("presetId"), Index("playerId")]
)
data class GamePresetPlayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: Long,
    val playerId: String
)
