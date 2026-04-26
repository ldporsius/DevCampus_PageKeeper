# PageKeeper — Book Detail Feature

## The core tension

A book reader has to handle content that is too large to hold in memory, yet must feel instant and seamless to the user.

The challenge is this: the scroll system needs to know the full structure of the book at all times — so the user can jump anywhere and the scrollbar behaves correctly — but you cannot actually load all that content. On top of that, reading position must survive everything: closing the app, rotating the screen, changing the font. Each of those is a different kind of state loss, and each needs its own strategy.

The solution is built around two ideas: **lazy structure with eager presence**, and **position expressed in terms that survive the context changing around them**.

---

## Lazy structure with eager presence

Every section is registered in the map before it is loaded. The `LazyColumn` always sees the full book — correct item count, correct keys, correct scroll range — but most entries are just shells.

```kotlin
val loadingPages = (0 until totalSections).associate { i -> i to Page.Loading(i) }
_state.update { it.copy(pages = loadingPages, currentSection = initialSection) }
```

Loading is triggered only when a placeholder actually enters the viewport:

```kotlin
is Page.Loading -> {
    LaunchedEffect(page.sectionId) {
        onAction(BookDetailAction.LoadSection(page.sectionId))
    }
    Spacer(modifier = Modifier.height(400.dp))
}
```

The `Spacer` is not cosmetic. Without a minimum height, all placeholders collapse to zero, the entire book becomes "visible" at once, and every section fires `LoadSection` simultaneously — a coroutine flood that freezes the UI. The height is a rate-limiter.

---

## Eviction keeps memory bounded

Once a section is far enough from the viewport, it is reset to `Page.Loading`. The list still knows it exists; it just unloads the content. Scrolling back into it re-triggers the `LaunchedEffect` and reloads cleanly.

```kotlin
val evicted = pages.mapValues { (id, page) ->
    if (page !is Page.Loading && abs(id - currentSection) > EVICTION_WINDOW)
        Page.Loading(id)
    else
        page
}
```

---

## Three-tier position persistence

### 1. Cross-session — Room database

Section index and pixel offset are written to Room on every scroll event (debounced to avoid write storms), and restored on cold start.

```kotlin
// saving
snapshotFlow {
    val sectionId = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key as? Int ?: -1
    sectionId to listState.firstVisibleItemScrollOffset
}.debounce(500).collect { (sectionId, offset) ->
    onAction(BookDetailAction.PlaceBookmark(sectionId, offset))
}

// restoring
listState.scrollToItem(
    index = state.currentSection.coerceAtMost(state.pages.size - 1),
    scrollOffset = state.currentSectionOffset
)
```

### 2. Cross-rotation — ratio-based anchor

A raw pixel offset is meaningless after the layout reflows to a new column width. Instead, an `anchorRatio` captures how far into the first visible item the viewport edge sits. That ratio is geometry-independent and survives rotation. `rememberSaveable` keeps it alive across the configuration change.

```kotlin
var anchorIndex by rememberSaveable { mutableIntStateOf(0) }
var anchorRatio by rememberSaveable { mutableFloatStateOf(0f) }

// tracking
snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
    .collect { (index, offset) ->
        val itemSize = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }?.size ?: return@collect
        anchorIndex = index
        anchorRatio = if (itemSize > 0) offset.toFloat() / itemSize else 0f
    }

// restoring after rotation
LaunchedEffect(configuration.orientation) {
    if (!scrollSettled) return@LaunchedEffect
    val layout = snapshotFlow { listState.layoutInfo }
        .first { it.visibleItemsInfo.any { it.index == anchorIndex } }
    val itemSize = layout.visibleItemsInfo
        .firstOrNull { it.index == anchorIndex }?.size ?: return@LaunchedEffect
    listState.scrollToItem(anchorIndex, (anchorRatio * itemSize).toInt())
}
```

### 3. Font-size change

A font-size change is just another reflow. The same ratio-based reanchor applies.

```kotlin
LaunchedEffect(readingSettings.fontSize) {
    val layout = snapshotFlow { listState.layoutInfo }
        .first { it.visibleItemsInfo.any { it.index == anchorIndex } }
    val itemSize = layout.visibleItemsInfo
        .firstOrNull { it.index == anchorIndex }?.size ?: return@LaunchedEffect
    listState.scrollToItem(anchorIndex, (anchorRatio * itemSize).toInt())
}
```

---

## The `scrollSettled` gate

All three restoration paths share one invariant: none of them should fire before the initial DB restore has run. A single boolean in `rememberSaveable` enforces this. It is `false` on cold start, `true` forever after — including across rotation — so the DB restore never overrides the ratio-based restore during a live session.

```kotlin
var scrollSettled by rememberSaveable { mutableStateOf(false) }

LaunchedEffect(state.pages, state.currentSection) {
    if (!scrollSettled && state.pages.isNotEmpty() && state.currentSection >= 0) {
        listState.scrollToItem(...)
        scrollSettled = true
    }
}
```

---

## Summary

The whole thing is a set of concentric guards: structure before content, ratio before pixels, session state before process state. Each guard handles exactly the failure mode it was designed for and nothing more.

| Threat | Strategy |
|---|---|
| Book too large for memory | Pre-populated `Page.Loading` map + eviction window |
| Coroutine flood on scroll | Minimum item height as rate-limiter |
| App closed and reopened | Section + offset persisted in Room |
| Screen rotated | `anchorRatio` in `rememberSaveable` + orientation `LaunchedEffect` |
| Font size changed | Same ratio-based reanchor |
| Restore order conflicts | `scrollSettled` gate shared across all paths |