package nl.codingwithlinda.pagekeeper.core.data.remote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FN2BookParserTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val parser = FN2BookParser(context)

    @Test
    fun testParse(): Unit = runBlocking{
        parser.fromAssets()
    }
}