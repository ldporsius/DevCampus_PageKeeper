package nl.codingwithlinda.pagekeeper.core.di

import android.app.Application

class PageKeeperApplication: Application() {


    companion object{
        lateinit var appModule: IAppModule
    }

    override fun onCreate() {
        super.onCreate()

        appModule = AppModule(this)
    }
}