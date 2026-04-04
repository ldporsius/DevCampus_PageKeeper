package nl.codingwithlinda.pagekeeper.core.data.util

import android.graphics.Bitmap


// Iteratively halves the bitmap while its largest dimension is still > maxPx * 2,
// then does one final precise scale. At most log₂(original/maxPx) iterations,
// each working on a progressively smaller image.
fun scaleCoverBitmapTo(original: Bitmap, maxPx: Int): Bitmap {
    if (maxOf(original.width, original.height) <= maxPx) return original

    var current = original
    while (maxOf(current.width, current.height) > maxPx * 2) {
        val halved = Bitmap.createScaledBitmap(current, current.width / 2, current.height / 2, true)
        if (current !== original) current.recycle()
        current = halved
    }

    val maxDim = maxOf(current.width, current.height)
    val scale = maxPx.toFloat() / maxDim
    val scaled = Bitmap.createScaledBitmap(
        current,
        (current.width * scale).toInt().coerceAtLeast(1),
        (current.height * scale).toInt().coerceAtLeast(1),
        true
    )
    if (current !== original) current.recycle()
    return scaled
}
