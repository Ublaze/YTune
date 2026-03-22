package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.BrowseResponse
import com.github.innertube.models.MusicTwoRowItemRenderer
import com.github.innertube.models.bodies.BrowseBody
import com.github.innertube.utils.from
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun Innertube.libraryPlaylists() = runCatchingNonCancellable {
    val response = client.post(BROWSE) {
        setBody(
            BrowseBody(browseId = "FEmusic_liked_playlists")
        )
    }.body<BrowseResponse>()

    val items = response
        .contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents
        ?.firstOrNull()
        ?.gridRenderer
        ?.items
        ?.mapNotNull { it.musicTwoRowItemRenderer }
        ?.mapNotNull(Innertube.PlaylistItem::from)

    items ?: emptyList()
}
