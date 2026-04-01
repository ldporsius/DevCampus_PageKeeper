package nl.codingwithlinda.pagekeeper.design_system.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class RememberDeviceConfigTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testFormFactors(){

        composeRule.setContent {
            Box(modifier = Modifier.fillMaxSize()){
                val config = rememberDeviceConfig()

                println("${"*".repeat(100)}")
                println("${"*".repeat(100)}")
                println(config)
                println("${"*".repeat(100)}")
                println("${"*".repeat(100)}")
            }
        }

    }
}