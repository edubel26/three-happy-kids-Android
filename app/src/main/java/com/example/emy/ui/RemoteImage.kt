package com.example.emy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import android.util.Log
import com.example.emy.R
import com.example.emy.data.remote.resolveImageUrl

@Composable
fun RemoteImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cacheKey: String? = null
) {
    val imageUrl = resolveImageUrl(imagePath)
    if (imageUrl == null) {
        ImageFallback(
            contentDescription = contentDescription,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .memoryCacheKey(cacheKey?.let { "$imageUrl@$it" } ?: imageUrl)
        .diskCacheKey(cacheKey?.let { "$imageUrl@$it" } ?: imageUrl)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .listener(onError = { _, result ->
            Log.w(
                "RemoteImage",
                "Image request failed: ${result.throwable.javaClass.simpleName}",
                result.throwable
            )
        })
        .build()

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(R.drawable.image_placeholder),
        error = painterResource(R.drawable.image_placeholder),
        fallback = painterResource(R.drawable.image_placeholder)
    )
}

@Composable
private fun ImageFallback(
    contentDescription: String?,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.image_placeholder),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
