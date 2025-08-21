package de.michael.tolleapp.di

import androidx.room.Room
import de.michael.tolleapp.presentation.app1.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.data.PlayerRepository
import de.michael.tolleapp.data.SkyjoDatabase
import de.michael.tolleapp.presentation.statistics.StatViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get()) }
    viewModel { StatViewModel(get()) }

    // Room database (singleton)
    single {
        Room.databaseBuilder(
                androidContext(),
                SkyjoDatabase::class.java,
                "skyjo.db"
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    // DAOs
    single { get<SkyjoDatabase>().playerDao() }
    single { get<SkyjoDatabase>().roundResultDao() }

    // Repository
    single { PlayerRepository(get(), get()) }
}
