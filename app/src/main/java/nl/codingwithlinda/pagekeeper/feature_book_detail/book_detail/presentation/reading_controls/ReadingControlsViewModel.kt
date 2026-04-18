package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.lifecycle.ViewModel
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction

class ReadingControlsViewModel: ViewModel() {

    fun onAction(action: ReadingControlAction){
        when(action){
            is ReadingControlAction.AdjustFontSize -> {
                //TODO
            }
            ReadingControlAction.ToggleAutoRotate -> {
                //TODO
            }
        }

        }

}