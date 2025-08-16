package de.michael.tolleapp.di

import androidx.room.Room
import de.michael.tolleapp.presentation.app1.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainViewModel
import de.michael.tolleapp.data.PlayerRepository
import de.michael.tolleapp.data.SkyjoDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Room database (singleton)
    single {
        Room.databaseBuilder(
            androidContext(),
            SkyjoDatabase::class.java,
            "skyjo.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    single { get<SkyjoDatabase>().playerDao() }
    single { get<SkyjoDatabase>().roundResultDao() }

    // Repository
    single { PlayerRepository(get(), get()) }

    // ViewModels
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel(get()) }
}
