package de.michael.tolleapp.di

import DartGameRepository
import androidx.room.Room
import de.michael.tolleapp.data.AppDatabase
import de.michael.tolleapp.data.player.PlayerRepository
import de.michael.tolleapp.data.games.schwimmen.game.SchwimmenGameRepository
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStatsRepository
import de.michael.tolleapp.data.games.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.data.games.presets.GamePresetRepository
import de.michael.tolleapp.data.games.skyjo.stats.SkyjoStatsRepository
import de.michael.tolleapp.data.settings.SettingsRepository
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.presentation.schwimmen.SchwimmenViewModel
import de.michael.tolleapp.presentation.settings.SettingsViewModel
import de.michael.tolleapp.presentation.skyjo.SkyjoViewModel
import de.michael.tolleapp.presentation.statistics.StatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.michael.tolleapp.presentation.dart.DartViewModel

val appModule = module {

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Spalte gameStyle hinzufügen, wenn sie fehlt
            database.execSQL("ALTER TABLE dart_games ADD COLUMN `gameStyle` INTEGER")

            // falls du sicherstellen willst, dass die Runden-Tabelle existiert:
            database.execSQL("""
            CREATE TABLE IF NOT EXISTS `dart_game_rounds` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `gameId` TEXT NOT NULL,
                `playerId` TEXT NOT NULL,
                `roundIndex` INTEGER NOT NULL,
                `dart1` INTEGER NOT NULL,
                `dart2` INTEGER NOT NULL,
                `dart3` INTEGER NOT NULL,
                FOREIGN KEY(`gameId`) REFERENCES `dart_games`(`id`) ON DELETE CASCADE
            )
        """)

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_dart_game_rounds_gameId` ON `dart_game_rounds`(`gameId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_dart_game_rounds_playerId` ON `dart_game_rounds`(`playerId`)")
        }
    }



    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app.db"
        )
            .addMigrations(MIGRATION_13_14) // <-- add migration here
            .build()
    }


    // DAOs
    single { get<AppDatabase>().playerDao() } // Player
    single { get<AppDatabase>().settingsDao() } // Settings
    single { get<AppDatabase>().gamePresetDao() } // Presets

    single { get<AppDatabase>().skyjoStatsDao() } // Skyjo
    single { get<AppDatabase>().skyjoRoundResultDao() }
    single { get<AppDatabase>().skyjoGameDao() }
    single { get<AppDatabase>().skyjoGameRoundDao() }

    single { get<AppDatabase>().schwimmenStatsDao() } // Schwimmen
    single { get<AppDatabase>().schwimmenGameDao() }
    single { get<AppDatabase>().schwimmenGameRoundDao() }

    // Dart
    single { get<AppDatabase>().dartGameDao() } // Dart


    // Repositories
    single { PlayerRepository(get()) } // Player
    single { SettingsRepository(get()) } // Settings

    single { SkyjoStatsRepository(get(), get()) } // Skyjo
    single { SkyjoGameRepository(get(), get()) }

    single { GamePresetRepository(get()) } // Presets

    single { SchwimmenStatsRepository(get(), get()) } // Schwimmen
    single { SchwimmenGameRepository(get(), get()) }

    single { DartGameRepository(get()) } // Dart


    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get(), get()) }
    viewModel { StatViewModel(get(), get()) }
    viewModel { SchwimmenViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { DartViewModel(get(), get(), get())}
}
