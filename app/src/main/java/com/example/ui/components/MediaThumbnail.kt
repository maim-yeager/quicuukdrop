package com.example.ui.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.FileCategory
import com.example.data.model.SharedFile
import com.example.data.model.formatDuration
import com.example.ui.theme.quickDropColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// In-memory cache so repeated composition does not re-decode video frames
private object VideoThumbnailCache {
    private const val MAX_ENTRIES = 40
    private val cache = LruCache<String, Bitmap>(MAX_ENTRIES)

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
}

/**
 * Generates a representative video frame asynchronously (cached by file id).
 * Falls back to null on any failure so callers can render a placeholder.
 */
@Composable
fun rememberVideoThumbnail(file: SharedFile): Bitmap? {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, file.id, file.uri) {
        val cached = VideoThumbnailCache.get(file.id)
        if (cached != null) {
            value = cached
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, file.uri)
                    val frame = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    frame?.let { downscale(it, 512) }
                } finally {
                    runCatching { retriever.release() }
                }
            }.getOrNull()
        }
        value?.let { VideoThumbnailCache.put(file.id, it) }
    }
    return bitmap
}

private fun downscale(src: Bitmap, maxDim: Int): Bitmap {
    val width = src.width
    val height = src.height
    if (width <= maxDim && height <= maxDim) return src
    val scale = maxDim.toFloat() / maxOf(width, height)
    val newWidth = (width * scale).toInt().coerceAtLeast(1)
    val newHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
}

/**
 * Liquid Glass media thumbnail. Shows real previews for images and videos
 * (with a play overlay and duration badge), and a category icon otherwise.
 * Handles missing / invalid media gracefully with a fallback placeholder.
 */
@Composable
fun MediaThumbnail(
    file: SharedFile,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val colors = quickDropColors()
    val context = LocalContext.current
    val imageRequest = remember(file.uri) {
        ImageRequest.Builder(context)
            .data(file.uri)
            .size(240)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        when {
            file.category == FileCategory.IMAGE -> {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        MediaLoadingPlaceholder()
                    },
                    error = {
                        MediaFallbackPlaceholder()
                    }
                )
            }

            file.category == FileCategory.VIDEO -> {
                val thumbnail = rememberVideoThumbnail(file)
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = file.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    MediaLoadingPlaceholder()
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Duration badge
                if (file.durationMs > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xB3000000))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = formatDuration(file.durationMs),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(file.category.color.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = file.category.icon,
                        contentDescription = file.category.title,
                        tint = file.category.color,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaLoadingPlaceholder() {
    val colors = quickDropColors()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.glassSurface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.accentCyan,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun MediaFallbackPlaceholder() {
    val colors = quickDropColors()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.glassSurface),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ImageNotSupported,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(24.dp)
        )
    }
}
