package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.data.games.dart.DartGame
import de.michael.tolleapp.data.games.dart.DartGameDao
import de.michael.tolleapp.data.games.dart.DartGameRound
import de.michael.tolleapp.data.games.dart.DartThrowDao
import de.michael.tolleapp.data.games.dart.DartThrowData
import de.michael.tolleapp.data.player.Player
import de.michael.tolleapp.data.player.PlayerDao
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGame
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameDao
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameRound
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameRoundDao
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStatsDao
import de.michael.tolleapp.data.games.skyjo.SkyjoGame
import de.michael.tolleapp.data.games.skyjo.SkyjoGameDao
import de.michael.tolleapp.data.games.skyjo.SkyjoGameRound
import de.michael.tolleapp.data.games.skyjo.SkyjoGameRoundDao
import de.michael.tolleapp.data.games.presets.GamePreset
import de.michael.tolleapp.data.games.presets.GamePresetDao
import de.michael.tolleapp.data.games.presets.GamePresetPlayer
import de.michael.tolleapp.data.games.skyjo.SkyjoGameLoser
import de.michael.tolleapp.data.games.skyjo.SkyjoGameStatisticsDao
import de.michael.tolleapp.data.games.skyjo.SkyjoGameWinner
import de.michael.tolleapp.data.settings.Settings
import de.michael.tolleapp.data.settings.SettingsDao

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
    version = 18,
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
