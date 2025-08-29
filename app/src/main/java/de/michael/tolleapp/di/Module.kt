package de.michael.tolleapp.di

import androidx.room.Room
import de.michael.tolleapp.data.AppDatabase
import de.michael.tolleapp.data.player.PlayerRepository
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.data.skyjo.player.SkyjoStatsRepository
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.presentation.skyjo.SkyjoViewModel
import de.michael.tolleapp.presentation.statistics.StatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Single AppDatabase instance
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app.db"
        )
            .fallbackToDestructiveMigration() // for dev/testing
            .build()
    }

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get()) }
    viewModel { StatViewModel(get()) }

    // DAOs
    single { get<AppDatabase>().playerDao() }
    single { get<AppDatabase>().skyjoStatsDao() }
    single { get<AppDatabase>().roundResultDao() }
    single { get<AppDatabase>().gameDao() }
    single { get<AppDatabase>().gameRoundDao() }

    // Repositories
    single { PlayerRepository(get()) }
    single { SkyjoStatsRepository(get(), get()) }
    single { SkyjoGameRepository(get(), get()) }

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get()) }
    viewModel { StatViewModel(get()) }
}
