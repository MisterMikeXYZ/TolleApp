package de.michael.tolleapp.di


import de.michael.tolleapp.presentation.app1.SkyjoViewModel
import de.michael.tolleapp.presentation.main.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Define your app-level dependencies here
    viewModel { MainViewModel() }
    viewModel { SkyjoViewModel() }
}