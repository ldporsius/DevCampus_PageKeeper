package nl.codingwithlinda.pagekeeper.feature_book_detail.book_detail.presentation.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PaginationScrollListener(
    listState: LazyListState,
    itemCount: Int,
    isPaginationLoading: Boolean,
    isEndReached: Boolean,
    onNearTop: () -> Unit,
) {
    val updatedItemCount by rememberUpdatedState(itemCount)
    val isPaginationLoading by rememberUpdatedState(isPaginationLoading)
    val isEndReached by rememberUpdatedState(isEndReached)

    var lastTriggeredItemCount by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val totalItems = info.totalItemsCount
            val topVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index
            val remainingItems = if (topVisibleIndex != null) {totalItems - topVisibleIndex -1} else null

            PaginationScrollState(
                currentItemCount = totalItems,
                isEligible = remainingItems != null
                        && remainingItems < 5
                        && !isPaginationLoading
                        && !isEndReached
            )
        }.distinctUntilChanged()
            .collect { (count, eligible) ->

                val shouldTrigger = eligible && count > lastTriggeredItemCount
                if (shouldTrigger){
                    lastTriggeredItemCount = count
                    onNearTop()
                }

        }
    }
}

data class PaginationScrollState(
    val currentItemCount: Int,
    val isEligible: Boolean
)