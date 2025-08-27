package de.michael.tolleapp.di

import androidx.room.Room
import de.michael.tolleapp.data.skyjo.game.SkyjoGameDatabase
import de.michael.tolleapp.data.skyjo.game.SkyjoGameRepository
import de.michael.tolleapp.presentation.app1.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.data.skyjo.player.PlayerRepository
import de.michael.tolleapp.data.skyjo.player.SkyjoPlayerDatabase
import de.michael.tolleapp.presentation.statistics.StatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Viewmodels, Room databases, DAOs and Repositories have to be handled
val appModule = module {

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get(), get()) }   // needs PlayerRepo + GameRepo
    viewModel { StatViewModel(get()) }

    // Room databases (singletons)
    single {
        Room.databaseBuilder(
            androidContext(),
            SkyjoPlayerDatabase::class.java,
            "skyjo.db"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            SkyjoGameDatabase::class.java,
            "skyjo_game.db"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    // DAOs
    single { get<SkyjoPlayerDatabase>().playerDao() }
    single { get<SkyjoPlayerDatabase>().roundResultDao() }
    single { get<SkyjoGameDatabase>().gameDao() }
    single { get<SkyjoGameDatabase>().gameRoundDao() }

    // Repositories
    single { PlayerRepository(get()) }
    single { SkyjoGameRepository(get(), get()) }
}
