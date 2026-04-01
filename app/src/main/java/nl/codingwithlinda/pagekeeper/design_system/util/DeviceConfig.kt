package nl.codingwithlinda.pagekeeper.design_system.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
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

    val isLargeWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val isMediumWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isMediumHeight = windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)


    val deviceType = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> DeviceType.Phone
        else -> DeviceType.Tablet
    }

    val orientation = when (windowSizeClass.windowHeightSizeClass) {
        WindowHeightSizeClass.COMPACT -> Orientation.Landscape
        else -> Orientation.Portrait
    }

    return DeviceConfig(deviceType, orientation)
}