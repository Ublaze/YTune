package com.github.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val draft: Boolean,
    val name: String,
    val prerelease: Boolean,
    @SerialName("tag_name")
    val tagName: String? = null,
    val assets: List<Asset> = emptyList()
) {
    // Release titles are free text; the tag (vX.Y.Z) is the reliable version source.
    val version: String
        get() = tagName ?: name

    @Serializable
    data class Asset(
        val name: String,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        val size: Long = 0
    )

    val apkDownloadUrl: String?
        get() = assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl
}
