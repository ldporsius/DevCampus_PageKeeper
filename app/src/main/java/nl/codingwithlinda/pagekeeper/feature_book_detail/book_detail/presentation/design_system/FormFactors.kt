package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.design_system

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.DeviceType
import nl.codingwithlinda.pagekeeper.core.presentation.design_system.util.rememberDeviceConfig
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControl
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlItem

@Composable
fun PhoneLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier
        .then(Modifier
            .padding(horizontal = 12.dp)

        )
    ) {
        content()
    }
}

@Composable
fun TabletLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier
        .then(Modifier
            .fillMaxSize()
        ),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                        .widthIn(480.dp, 600.dp)
                        .padding(horizontal = 12.dp)
            ,
            contentAlignment = Alignment.TopCenter
        ) {
            content()
        }
    }
}



@Composable
fun FormFactorWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    ) {
    val deviceConfig = rememberDeviceConfig()

    when(deviceConfig.deviceType){
        DeviceType.Phone -> PhoneLayout(modifier = modifier) {
            content()
        }
        DeviceType.Tablet -> TabletLayout(modifier = modifier) {
            content()
        }
    }
}

fun DeviceType.toReadingControls(): List<ReadingControl> = when(this){
    DeviceType.Phone -> listOf(
        ReadingControl.AUTO_ROTATE, ReadingControl.FONT_SIZE
    )
    DeviceType.Tablet -> {
        listOf(
            ReadingControl.FONT_SIZE
        )
    }
}