package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.BrowseResponse
import com.github.innertube.models.ContinuationResponse
import com.github.innertube.models.GridRenderer
import com.github.innertube.models.bodies.BrowseBody
import com.github.innertube.models.bodies.ContinuationBody
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
        mask("contents.singleColumnBrowseResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.gridRenderer(items.$MUSIC_TWO_ROW_ITEM_RENDERER_MASK,continuations)")
    }.body<BrowseResponse>()

    val gridRenderer = response
        .contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents
        ?.firstNotNullOfOrNull { it.gridRenderer }

    val items = gridRenderer.toPlaylistItems().toMutableList()
    var continuation = gridRenderer.nextContinuation

    while (continuation != null) {
        val currentContinuation = continuation
        val nextGridRenderer = client.post(BROWSE) {
            setBody(ContinuationBody(continuation = currentContinuation))
            mask("continuationContents.gridContinuation(items.$MUSIC_TWO_ROW_ITEM_RENDERER_MASK,continuations)")
        }.body<ContinuationResponse>()
            .continuationContents
            ?.gridContinuation
            ?: break

        items += nextGridRenderer.toPlaylistItems()

        val nextContinuation = nextGridRenderer.nextContinuation
        if (nextContinuation == currentContinuation) break
        continuation = nextContinuation
    }

    items.distinctBy(Innertube.PlaylistItem::key)
}

private val GridRenderer?.nextContinuation
    get() = this
        ?.continuations
        ?.firstOrNull()
        ?.nextContinuationData
        ?.continuation

private fun GridRenderer?.toPlaylistItems() =
    this
        ?.items
        ?.mapNotNull { it.musicTwoRowItemRenderer }
        ?.mapNotNull(Innertube.PlaylistItem::from)
        ?: emptyList()
