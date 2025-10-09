package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.games.dart.data.*
import de.michael.tolleapp.games.flip7.data.Flip7Dao
import de.michael.tolleapp.games.flip7.data.Flip7StatisticsDao
import de.michael.tolleapp.games.flip7.data.entities.Flip7GameEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7PlayerEntity
import de.michael.tolleapp.games.flip7.data.entities.Flip7RoundEntity
import de.michael.tolleapp.games.romme.data.RommeDao
import de.michael.tolleapp.games.romme.data.entities.RommeGameEntity
import de.michael.tolleapp.games.romme.data.entities.RommeGamePlayerEntity
import de.michael.tolleapp.games.romme.data.entities.RommeRoundEntity
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGame
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameDao
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameRound
import de.michael.tolleapp.games.schwimmen.data.game.SchwimmenGameRoundDao
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStatsDao
import de.michael.tolleapp.games.skyjo.data.SkyjoDao
import de.michael.tolleapp.games.skyjo.data.SkyjoGameStatisticsDao
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoGameEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoPlayerEntity
import de.michael.tolleapp.games.skyjo.data.entities.SkyjoRoundEntity
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.player.PlayerDao
import de.michael.tolleapp.games.util.presets.GamePreset
import de.michael.tolleapp.games.util.presets.GamePresetDao
import de.michael.tolleapp.games.util.presets.GamePresetPlayer
import de.michael.tolleapp.games.wizard.data.WizardDao
import de.michael.tolleapp.games.wizard.data.entities.WizardGameEntity
import de.michael.tolleapp.games.wizard.data.entities.WizardGamePlayerEntity
import de.michael.tolleapp.games.wizard.data.entities.WizardRoundEntity
import de.michael.tolleapp.settings.data.Settings
import de.michael.tolleapp.settings.data.SettingsDao

@Database(
    entities = [
        Player::class,
        Settings::class,

        GamePreset::class,
        GamePresetPlayer::class,

        // Skyjo
        SkyjoGameEntity::class,
        SkyjoRoundEntity::class,
        SkyjoPlayerEntity::class,

        // Schwimmen
        SchwimmenStats::class,
        SchwimmenGame::class,
        SchwimmenGameRound::class,

        // Dart
        DartGame::class,
        DartGameRound::class,
        DartThrowData::class,

        // Wizard
        WizardGameEntity::class,
        WizardGamePlayerEntity::class,
        WizardRoundEntity::class,

        // Romme
        RommeGameEntity::class,
        RommeGamePlayerEntity::class,
        RommeRoundEntity::class,

        // Flip7
        Flip7GameEntity::class,
        Flip7PlayerEntity::class,
        Flip7RoundEntity::class,
    ],
    version = 28,
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
    abstract fun skyjoGameDao(): SkyjoDao
    abstract fun skyjoGameStatistics(): SkyjoGameStatisticsDao

    // Schwimmen
    abstract fun schwimmenStatsDao(): SchwimmenStatsDao
    abstract fun schwimmenGameDao(): SchwimmenGameDao
    abstract fun schwimmenGameRoundDao(): SchwimmenGameRoundDao

    // Dart
    abstract fun dartGameDao(): DartGameDao
    abstract fun dartThrowDao(): DartThrowDao

    // Wizard
    abstract fun wizardDao(): WizardDao

    // Rommé
    abstract fun rommeDao(): RommeDao

    // Flip7
    abstract fun flip7Dao(): Flip7Dao
    abstract fun flip7StatisticsDao(): Flip7StatisticsDao
}
