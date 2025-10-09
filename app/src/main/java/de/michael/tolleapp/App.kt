package de.michael.tolleapp

import android.app.Application
import de.michael.tolleapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Guard so tests / process restarts don't double-start
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@App)
                modules(appModule)
            }
        }
    }
}
