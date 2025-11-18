package karika.distribucija.ba.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import okio.FileSystem
import kotlin.math.ceil
import kotlin.math.round

fun karikaPriceFormat(value: Double): String {
    val scaled = round(value * 100) / 100
    val parts = scaled.toString().split('.')

    val integerPart = parts[0]
    val decimalPart = when (parts.getOrNull(1)?.length) {
        0 -> "00"
        1 -> parts[1] + "0"
        2 -> parts[1]
        else -> parts[1].take(2)
    }
    return "$integerPart,$decimalPart"
}

fun String.addConditionally(valid: Boolean, value: String): String {
    return when (valid) {
        false -> this
        else -> this.plus(value)
    }
}

fun PlatformContext.asyncImageLoader() =
    ImageLoader
        .Builder(this)
        .components { add(KtorNetworkFetcherFactory()) }
        .crossfade(true)
        .networkCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(this, 0.25)
                .strongReferencesEnabled(true)
                .build()
        }
        // .logger(DebugLogger())
        .build()

/**
 * Enable disk cache for the [ImageLoader].
 */
fun ImageLoader.enableDiskCache() = this.newBuilder()
    .diskCache {
        DiskCache.Builder()
            .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
            .build()
    }.build()
