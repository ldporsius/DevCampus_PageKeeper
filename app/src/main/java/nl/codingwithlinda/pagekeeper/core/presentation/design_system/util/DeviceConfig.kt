package nl.codingwithlinda.pagekeeper.core.presentation.design_system.util

import android.content.res.Configuration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

enum class DeviceType { Phone, Tablet }
enum class Orientation { Portrait, Landscape }

data class DeviceConfig(
    val deviceType: DeviceType,
    val orientation: Orientation
)

@Composable
fun rememberDeviceConfig(): DeviceConfig {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val configuration = LocalConfiguration.current

    // A phone is compact in at least one dimension.
    // Phones in portrait: width=COMPACT, height=MEDIUM/EXPANDED
    // Phones in landscape: width=MEDIUM, height=COMPACT
    // Tablets: width=MEDIUM/EXPANDED and height=MEDIUM/EXPANDED
    val deviceType = when {
        windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT -> DeviceType.Phone
        windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT -> DeviceType.Phone
        else -> DeviceType.Tablet
    }

    val orientation = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> Orientation.Landscape
        else -> Orientation.Portrait
    }

    return DeviceConfig(deviceType, orientation)
}