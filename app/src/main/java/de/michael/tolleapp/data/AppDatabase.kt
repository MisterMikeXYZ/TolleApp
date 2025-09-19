package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.games.dart.data.DartGame
import de.michael.tolleapp.games.dart.data.DartGameDao
import de.michael.tolleapp.games.dart.data.DartGameRound
import de.michael.tolleapp.games.dart.data.DartThrowDao
import de.michael.tolleapp.games.dart.data.DartThrowData
import de.michael.tolleapp.games.player.Player
import de.michael.tolleapp.games.player.PlayerDao
import de.michael.tolleapp.games.presets.GamePreset
import de.michael.tolleapp.games.presets.GamePresetDao
import de.michael.tolleapp.games.presets.GamePresetPlayer
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGame
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameDao
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameRound
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameRoundDao
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStatsDao
import de.michael.tolleapp.games.skyjo.data.SkyjoGame
import de.michael.tolleapp.games.skyjo.data.SkyjoGameDao
import de.michael.tolleapp.games.skyjo.data.SkyjoGameLoser
import de.michael.tolleapp.games.skyjo.data.SkyjoGameRound
import de.michael.tolleapp.games.skyjo.data.SkyjoGameRoundDao
import de.michael.tolleapp.games.skyjo.data.SkyjoGameStatisticsDao
import de.michael.tolleapp.games.skyjo.data.SkyjoGameWinner
import de.michael.tolleapp.settings.data.settings.Settings
import de.michael.tolleapp.settings.data.settings.SettingsDao

@Database(
    entities = [
        Player::class,
        Settings::class,

        GamePreset::class,
        GamePresetPlayer::class,

        // Skyjo
        SkyjoGame::class,
        SkyjoGameRound::class,
        SkyjoGameWinner::class,
        SkyjoGameLoser::class,

        // Schwimmen
        SchwimmenStats::class,
        SchwimmenGame::class,
        SchwimmenGameRound::class,

        // Dart
        DartGame::class,
        DartGameRound::class,
        DartThrowData::class
    ],
    version = 19,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // Player
    abstract fun playerDao(): PlayerDao

    // Presets
    abstract fun gamePresetDao(): GamePresetDao

    // Settings
    abstract fun settingsDao(): SettingsDao

    // Skyjo
    abstract fun skyjoGameDao(): SkyjoGameDao
    abstract fun skyjoGameRoundDao(): SkyjoGameRoundDao
    abstract fun skyjoGameStatisticsDao(): SkyjoGameStatisticsDao

    // Schwimmen
    abstract fun schwimmenStatsDao(): SchwimmenStatsDao
    abstract fun schwimmenGameDao(): SchwimmenGameDao
    abstract fun schwimmenGameRoundDao(): SchwimmenGameRoundDao

    // Dart
    abstract fun dartGameDao(): DartGameDao
    abstract fun dartThrowDao(): DartThrowDao
}
