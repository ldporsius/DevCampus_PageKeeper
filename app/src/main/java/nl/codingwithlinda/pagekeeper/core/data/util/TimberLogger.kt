package nl.codingwithlinda.pagekeeper.core.data.util

import android.content.Context
import nl.codingwithlinda.pagekeeper.BuildConfig
import nl.codingwithlinda.pagekeeper.core.domain.util.Logger
import timber.log.Timber

class TimberLogger(
): Logger {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun log(message: String) {
        Timber.d(message)
    }
}