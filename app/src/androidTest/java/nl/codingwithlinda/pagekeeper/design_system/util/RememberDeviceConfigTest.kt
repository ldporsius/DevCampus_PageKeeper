package nl.codingwithlinda.pagekeeper.design_system.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import assertk.assertThat
import assertk.assertions.isEqualTo
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.Orientation
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import org.junit.Rule
import org.junit.Test

class RememberDeviceConfigTest {

    @get:Rule
    val composeRule = createComposeRule()
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())


    @Test
    fun testFormFactors(){

        device.setOrientationLandscape()
        composeRule.setContent {
            Box(modifier = Modifier.fillMaxSize()){
                val config = rememberDeviceConfig()

                println("${"*".repeat(100)}")
                println("${"*".repeat(100)}")
                println(config)
                println("${"*".repeat(100)}")
                println("${"*".repeat(100)}")

                assertThat(config.deviceType).isEqualTo(DeviceType.Phone)
                assertThat(config.orientation).isEqualTo(Orientation.Landscape)
            }
        }

    }
}