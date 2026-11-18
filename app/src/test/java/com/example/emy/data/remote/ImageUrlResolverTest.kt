package com.example.emy.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlResolverTest {
    private val backendUrl = "http://10.0.2.2:3000/"

    @Test
    fun resolvesRootRelativeImagePath() {
        assertEquals(
            "http://10.0.2.2:3000/media/productos/camiseta-123.webp",
            resolveImageUrl("/media/productos/camiseta-123.webp", backendUrl)
        )
    }

    @Test
    fun resolvesPathRelativeToBackendBase() {
        assertEquals(
            "http://10.0.2.2:3000/media/productos/camiseta-123.webp",
            resolveImageUrl("media/productos/camiseta-123.webp", backendUrl)
        )
    }

    @Test
    fun resolvesBareProductFilenameToSharedMediaDirectory() {
        assertEquals(
            "http://10.0.2.2:3000/media/productos/camiseta-123.webp",
            resolveImageUrl("camiseta-123.webp", backendUrl)
        )
    }

    @Test
    fun preservesAbsoluteExternalUrl() {
        assertEquals(
            "https://cdn.example.com/producto.webp",
            resolveImageUrl("https://cdn.example.com/producto.webp", backendUrl)
        )
    }

    @Test
    fun rewritesLoopbackAbsoluteUrlToConfiguredBackendHost() {
        assertEquals(
            "http://10.0.2.2:3000/media/productos/camiseta-123.webp",
            resolveImageUrl(
                "http://localhost:3000/media/productos/camiseta-123.webp",
                backendUrl
            )
        )
    }

    @Test
    fun resolvesBackendAssetsPath() {
        assertEquals(
            "http://10.0.2.2:3000/assets/productos/producto1.jpg",
            resolveImageUrl("/assets/productos/producto1.jpg", backendUrl)
        )
    }

    @Test
    fun rejectsMissingOrInvalidImagePaths() {
        assertNull(resolveImageUrl(null, backendUrl))
        assertNull(resolveImageUrl("  ", backendUrl))
        assertNull(resolveImageUrl("javascript:alert(1)", backendUrl))
        assertNull(resolveImageUrl("//cdn.example.com/producto.webp", backendUrl))
    }
}
