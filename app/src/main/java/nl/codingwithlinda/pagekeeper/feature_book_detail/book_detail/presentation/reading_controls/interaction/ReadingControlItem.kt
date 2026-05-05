package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction

import nl.codingwithlinda.pagekeeper.R
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.ReadingOrientation

enum class ReadingControl{
    CHAPTERS,
    AUTO_ROTATE,
    FONT_SIZE
}
interface ReadingControlItem {
    fun onAction()
    val icon: Int
    val contentDescription: String
    val text: String
}

data class ChaptersControl(
    val onAction: () -> Unit,
): ReadingControlItem {
    override fun onAction() {
        onAction.invoke()
    }
    override val icon: Int = R.drawable.chapters
    override val contentDescription: String = "chapters"
    override val text: String = "Chapters"
}

data class AutoRotateControl(
    val setting: ReadingOrientation,
    val onAction: () -> Unit,
): ReadingControlItem {
    override fun onAction() {
        onAction.invoke()
    }
    override val icon: Int = if(setting == ReadingOrientation.LOCKED_LANDSCAPE)
        R.drawable.landscape else R.drawable.portrait
    override val contentDescription: String = "auto_rotate"
    override val text: String = if(setting == ReadingOrientation.LOCKED_LANDSCAPE) "Landscape" else "Auto Rotate"
}

data class FontSizeControl(
    val onAction: () -> Unit,
): ReadingControlItem {
    override fun onAction() {
        onAction.invoke()
    }
    override val icon: Int = R.drawable.font_size
    override val contentDescription: String = "font_size"
    override val text: String = "Font Size"
}



