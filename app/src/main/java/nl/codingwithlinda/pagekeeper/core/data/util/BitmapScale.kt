package nl.codingwithlinda.pagekeeper.core.data.util

import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext


// Iteratively halves the bitmap while its largest dimension is still > maxPx * 2,
// then does one final precise scale. At most log₂(original/maxPx) iterations,
// each working on a progressively smaller image.
suspend fun scaleCoverBitmapTo(original: Bitmap, maxPx: Int): Bitmap = withContext(Dispatchers.Default){
    if (maxOf(original.width, original.height) <= maxPx) return@withContext original

    var current = original
    try {

        while (isActive && maxOf(current.width, current.height) > maxPx * 2) {
            val halved = current.scale(current.width / 2, current.height / 2)
            if (current !== original) current.recycle()
            current = halved
        }

        ensureActive()
        val maxDim = maxOf(current.width, current.height)
        val scale = maxPx.toFloat() / maxDim
        val scaled = current.scale(
            (current.width * scale).toInt().coerceAtLeast(1),
            (current.height * scale).toInt().coerceAtLeast(1)
        )
        if (current !== original) current.recycle()
        return@withContext scaled
    }catch (e: CancellationException){
        if (current !== original) current.recycle()
        throw e
    }
}
