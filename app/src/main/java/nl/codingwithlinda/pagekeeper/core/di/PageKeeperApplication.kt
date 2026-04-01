package nl.codingwithlinda.pagekeeper.core.di

import android.app.Application

class PageKeeperApplication: Application() {


    companion object{
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()

        appModule = AppModule()
    }
}