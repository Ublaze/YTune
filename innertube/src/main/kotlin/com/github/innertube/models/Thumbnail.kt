package com.github.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnail(
    val url: String,
    val height: Int?,
    val width: Int?
) {
    val isResizable: Boolean
        get() = !url.startsWith("https://i.ytimg.com")

    // Google-hosted images re-render server-side at the requested size;
    // i.ytimg.com video thumbs only exist in fixed variants, so upgrade to
    // hqdefault (480px, always available) instead of serving the tiny default.
    fun size(size: Int): String {
        return when {
            url.startsWith("https://lh3.googleusercontent.com") ||
                    url.startsWith("https://yt3.ggpht.com") ->
                "${url.substringBeforeLast('=')}=w$size-h$size-l90-rj"

            url.startsWith("https://i.ytimg.com/vi/") ->
                "https://i.ytimg.com/vi/${
                    url.substringAfter("/vi/").substringBefore('/')
                }/hqdefault.jpg"

            else -> url
        }
    }
}
