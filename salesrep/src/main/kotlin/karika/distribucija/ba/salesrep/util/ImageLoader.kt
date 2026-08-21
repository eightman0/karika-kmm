package karika.distribucija.ba.salesrep.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import karika.distribucija.ba.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Minimal network image loader (no Coil/Glide dependency available in :salesrep).
 * Mirrors composeApp's Coil-backed KarikaImage closely enough for product thumbnails:
 * fetch once, cache the decoded bitmap in memory, and guard against ViewHolder recycling
 * by checking the ImageView's tag still matches the requested url before applying it. */
private val bitmapCache = LruCache<String, Bitmap>(80)

fun ImageView.loadUrl(url: String?, owner: LifecycleOwner) {
    tag = url
    if (url.isNullOrBlank()) {
        setImageDrawable(null)
        return
    }
    bitmapCache.get(url)?.let {
        setImageBitmap(it)
        return
    }
    setImageDrawable(null)
    owner.lifecycleScope.launch {
        val bitmap = withContext(Dispatchers.IO) {
            try {
                (URL(url).openConnection() as HttpURLConnection).run {
                    connectTimeout = 8000
                    readTimeout = 8000
                    inputStream.use { BitmapFactory.decodeStream(it) }
                }
            } catch (e: Exception) {
                AppLogger.w("ImageLoader", "Failed to load $url: ${e.message}")
                null
            }
        }
        if (bitmap != null) {
            bitmapCache.put(url, bitmap)
            if (tag == url) setImageBitmap(bitmap)
        }
    }
}
