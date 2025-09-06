package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.data.games.dart.DartGame
import de.michael.tolleapp.data.games.dart.DartGameDao
import de.michael.tolleapp.data.games.dart.DartGameRound
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
import de.michael.tolleapp.data.games.presets.GamePreset
import de.michael.tolleapp.data.games.presets.GamePresetDao
import de.michael.tolleapp.data.games.presets.GamePresetPlayer
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoRoundResult
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoRoundResultDao
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStats
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStatsDao
import de.michael.tolleapp.data.settings.Settings
import de.michael.tolleapp.data.settings.SettingsDao

@Database(
    entities = [
        Player::class,
        Settings::class,

        GamePreset::class,
        GamePresetPlayer::class,

        SkyjoStats::class,
        SkyjoRoundResult::class,
        SkyjoGame::class,
        SkyjoGameRound::class,

        SchwimmenStats::class,
        SchwimmenGame::class,
        SchwimmenGameRound::class,

        DartGame::class,
        DartGameRound::class
    ],
    version = 14
)
abstract class AppDatabase : RoomDatabase() {

    // Player
    abstract fun playerDao(): PlayerDao

    // Presets
    abstract fun gamePresetDao(): GamePresetDao

    // Settings
    abstract fun settingsDao(): SettingsDao

    // Skyjo
    abstract fun skyjoStatsDao(): SkyjoStatsDao
    abstract fun skyjoRoundResultDao(): SkyjoRoundResultDao
    abstract fun skyjoGameDao(): SkyjoGameDao
    abstract fun skyjoGameRoundDao(): SkyjoGameRoundDao

    // Schwimmen
    abstract fun schwimmenStatsDao(): SchwimmenStatsDao
    abstract fun schwimmenGameDao(): SchwimmenGameDao
    abstract fun schwimmenGameRoundDao(): SchwimmenGameRoundDao

    // Dart
    abstract fun dartGameDao(): DartGameDao
}
