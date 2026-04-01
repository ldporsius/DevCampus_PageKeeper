package nl.codingwithlinda.pagekeeper.core.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PageKeeperApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PageKeeperApplication)
            modules(
                appDataModule,
                appPresentationModule
            )
        }
    }
}