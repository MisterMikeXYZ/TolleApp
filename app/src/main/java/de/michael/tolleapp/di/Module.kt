package de.michael.tolleapp.di

import androidx.room.Room
import de.michael.tolleapp.data.AppDatabase
import de.michael.tolleapp.data.player.PlayerRepository
import de.michael.tolleapp.data.schwimmen.game.SchwimmenGameRepository
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStatsRepository
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.data.skyjo.stats.SkyjoStatsRepository
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.presentation.schwimmen.SchwimmenViewModel
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

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get()) }
    viewModel { StatViewModel(get()) }

    // DAOs
    single { get<AppDatabase>().playerDao() }

    single { get<AppDatabase>().skyjoStatsDao() }
    single { get<AppDatabase>().skyjoRoundResultDao() }
    single { get<AppDatabase>().skyjoGameDao() }
    single { get<AppDatabase>().skyjoGameRoundDao() }

    single { get<AppDatabase>().schwimmenStatsDao() }
    single { get<AppDatabase>().schwimmenGamePlayerDao() }
    single { get<AppDatabase>().schwimmenGameDao() }
    single { get<AppDatabase>().roundPlayerDao() }

    // Repositories
    single { PlayerRepository(get()) }

    single { SkyjoStatsRepository(get(), get()) }
    single { SkyjoGameRepository(get(), get()) }

    single { SchwimmenStatsRepository(get(), get()) }
    single { SchwimmenGameRepository(get(), get(), get()) }

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get(), get()) }
    viewModel { StatViewModel(get()) }
    viewModel { SchwimmenViewModel(get(), get()) }
}
