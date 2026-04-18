package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import nl.codingwithlinda.pagekeeper.R

enum class ReadingOrientation {
    AUTO_ROTATE,
    LOCKED_LANDSCAPE
}

fun ReadingOrientation.toUi() = when(this){
    ReadingOrientation.AUTO_ROTATE -> ReadingOrientationUi(
        icon = R.drawable.portrait,
        text = "Auto Rotate"
    )
    ReadingOrientation.LOCKED_LANDSCAPE -> ReadingOrientationUi(
        icon = R.drawable.landscape,
        text = "Landscape"
    )
}


data class ReadingOrientationUi(
    @DrawableRes val icon: Int,
    val text: String
)

