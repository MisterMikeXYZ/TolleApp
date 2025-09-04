package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGame
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameDao
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameRound
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameRoundDao
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStatsDao
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGameRound
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGame
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGameDao
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGameRoundDao
import de.michael.tolleapp.data.games.skyjo.presets.SkyjoPreset
import de.michael.tolleapp.data.games.skyjo.presets.SkyjoPresetDao
import de.michael.tolleapp.data.games.skyjo.presets.SkyjoPresetPlayer
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoRoundResult
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoRoundResultDao
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStats
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStatsDao
import de.michael.tolleapp.data.settings.Settings
import de.michael.tolleapp.data.settings.SettingsDao

@Database(
    entities = [
        Player::class,
        SkyjoStats::class,
        SkyjoRoundResult::class,
        SkyjoGame::class,
        SkyjoPreset::class,
        SkyjoPresetPlayer::class,
        SkyjoGameRound::class,
        SchwimmenStats::class,
        SchwimmenGame::class,
        SchwimmenGameRound::class,
        Settings::class,
    ],
    version = 10
)
abstract class AppDatabase : RoomDatabase() {

    // Player
    abstract fun playerDao(): PlayerDao

    // Settings
    abstract fun settingsDao(): SettingsDao

    // Skyjo
    abstract fun skyjoStatsDao(): SkyjoStatsDao
    abstract fun skyjoRoundResultDao(): SkyjoRoundResultDao
    abstract fun skyjoGameDao(): SkyjoGameDao
    abstract fun skyjoGameRoundDao(): SkyjoGameRoundDao
    abstract fun skyjoPresetDao(): SkyjoPresetDao

    // Schwimmen
    abstract fun schwimmenStatsDao(): SchwimmenStatsDao
    abstract fun schwimmenGameDao(): SchwimmenGameDao
    abstract fun schwimmenGameRoundDao(): SchwimmenGameRoundDao
}
