package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettings
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.domain.ReadingSettingsRepository
import nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.reading_controls.interaction.ReadingControlAction

class ReadingControlsViewModel(
    private val readingSettingsRepository: ReadingSettingsRepository
): ViewModel() {

    val state = readingSettingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReadingSettings()
    )

    fun onAction(action: ReadingControlAction){
        when(action){
            is ReadingControlAction.AdjustFontSize -> {
                viewModelScope.launch {
                    println("--- READING CONTROLS --- ACTION: AdjustFontSize : ${action.factor}")

                    readingSettingsRepository.setFontSize(action.factor)
                }
            }
            ReadingControlAction.ToggleAutoRotate -> {
                viewModelScope.launch {
                    val next = when (state.value.orientation) {
                        ReadingOrientation.AUTO_ROTATE -> ReadingOrientation.LOCKED_LANDSCAPE
                        ReadingOrientation.LOCKED_LANDSCAPE -> ReadingOrientation.AUTO_ROTATE
                    }
                    readingSettingsRepository.setOrientation(next)
                }
            }
            is ReadingControlAction.SetCurrentSection -> {
                viewModelScope.launch {
                    readingSettingsRepository.setCurrentSection(action.section)
                }
            }
        }
    }
}