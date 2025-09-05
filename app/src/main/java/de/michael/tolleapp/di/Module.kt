package de.michael.tolleapp.di

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

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app.db"
        )
            .fallbackToDestructiveMigration()
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

    // Repositories
    single { PlayerRepository(get()) } // Player
    single { SettingsRepository(get()) } // Settings

    single { SkyjoStatsRepository(get(), get()) } // Skyjo
    single { SkyjoGameRepository(get(), get()) }

    single { GamePresetRepository(get()) } // Presets

    single { SchwimmenStatsRepository(get(), get()) } // Schwimmen
    single { SchwimmenGameRepository(get(), get()) }

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get(), get()) }
    viewModel { StatViewModel(get(), get()) }
    viewModel { SchwimmenViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
