package com.example.emy.data.remote

import com.example.emy.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private val urlScheme = Regex("^[A-Za-z][A-Za-z0-9+.-]*:")

fun resolveImageUrl(
    imagePath: String?,
    backendBaseUrl: String = BuildConfig.BACKEND_BASE_URL
): String? {
    val value = imagePath?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val absoluteUrl = value.toHttpUrlOrNull()
    if (absoluteUrl != null) {
        val backendUrl = backendBaseUrl.toHttpUrlOrNull()
            ?.takeIf { it.isHttpOrHttps() }
        if (backendUrl != null && absoluteUrl.host in loopbackHosts) {
            return absoluteUrl.newBuilder()
                .scheme(backendUrl.scheme)
                .host(backendUrl.host)
                .port(backendUrl.port)
                .build()
                .toString()
        }
        return absoluteUrl.toString()
    }
    if (value.startsWith("//") || urlScheme.containsMatchIn(value)) return null

    val backendUrl = backendBaseUrl.toHttpUrlOrNull()
        ?.takeIf { it.isHttpOrHttps() }
        ?: return null
    val backendPath = if ('/' !in value) "/media/productos/$value" else value
    return backendUrl.resolve(backendPath)
        ?.takeIf { it.isHttpOrHttps() }
        ?.toString()
}

private val loopbackHosts = setOf("localhost", "127.0.0.1", "::1")

private fun HttpUrl.isHttpOrHttps(): Boolean = scheme == "http" || scheme == "https"
