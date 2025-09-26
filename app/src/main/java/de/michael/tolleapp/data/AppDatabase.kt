package de.michael.tolleapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import de.michael.tolleapp.games.dart.data.DartGame
import de.michael.tolleapp.games.dart.data.DartGameDao
import de.michael.tolleapp.games.dart.data.DartGameRound
import de.michael.tolleapp.games.dart.data.DartThrowDao
import de.michael.tolleapp.games.dart.data.DartThrowData
import de.michael.tolleapp.games.romme.data.RommeDao
import de.michael.tolleapp.games.romme.data.entities.RommeGameEntity
import de.michael.tolleapp.games.romme.data.entities.RommeGamePlayerEntity
import de.michael.tolleapp.games.romme.data.entities.RommeRoundEntity
import de.michael.tolleapp.games.util.player.Player
import de.michael.tolleapp.games.util.player.PlayerDao
import de.michael.tolleapp.games.util.presets.GamePreset
import de.michael.tolleapp.games.util.presets.GamePresetDao
import de.michael.tolleapp.games.util.presets.GamePresetPlayer
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
        DartThrowData::class,

        // Wizard
        WizardGameEntity::class,
        WizardGamePlayerEntity::class,
        WizardRoundEntity::class,

        // Romme
        RommeGameEntity::class,
        RommeGamePlayerEntity::class,
        RommeRoundEntity::class,
    ],
    version = 21,
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

    // Wizard
    abstract fun wizardDao(): WizardDao

    // Rommé
    abstract fun rommeDao(): RommeDao
}
