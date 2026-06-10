package com.github.musicyou.utils

import android.content.Context
import androidx.core.content.edit
import com.github.innertube.Innertube
import com.github.innertube.models.NavigationEndpoint
import com.github.innertube.models.Thumbnail
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the user's YouTube Music playlists so the Playlists tab can show
 * them instantly (and offline) instead of waiting for the network on every
 * visit. The cache is refreshed whenever a live fetch succeeds.
 */

fun Context.loadYtmPlaylistsCache(): List<Innertube.PlaylistItem> = runCatching {
    val raw = preferences.getString(ytmPlaylistsCacheKey, null) ?: return emptyList()

    val array = JSONArray(raw)
    (0 until array.length()).mapNotNull { index ->
        val item = array.optJSONObject(index) ?: return@mapNotNull null
        val browseId = item.optString("browseId").takeIf { it.isNotBlank() }
            ?: return@mapNotNull null

        Innertube.PlaylistItem(
            info = Innertube.Info(
                name = item.optString("name").takeIf { it.isNotBlank() },
                endpoint = NavigationEndpoint.Endpoint.Browse(browseId = browseId)
            ),
            channel = null,
            songCount = item.optInt("songCount", -1).takeIf { it >= 0 },
            thumbnail = item.optString("thumbnailUrl").takeIf { it.isNotBlank() }?.let {
                Thumbnail(url = it, height = null, width = null)
            }
        )
    }
}.getOrDefault(emptyList())

fun Context.saveYtmPlaylistsCache(playlists: List<Innertube.PlaylistItem>) {
    val array = JSONArray()

    playlists.forEach { playlist ->
        val browseId = playlist.info?.endpoint?.browseId ?: return@forEach

        array.put(
            JSONObject().apply {
                put("browseId", browseId)
                playlist.info?.name?.let { put("name", it) }
                playlist.songCount?.let { put("songCount", it) }
                playlist.thumbnail?.url?.let { put("thumbnailUrl", it) }
            }
        )
    }

    preferences.edit { putString(ytmPlaylistsCacheKey, array.toString()) }
}

fun Context.clearYtmPlaylistsCache() {
    preferences.edit { remove(ytmPlaylistsCacheKey) }
}
