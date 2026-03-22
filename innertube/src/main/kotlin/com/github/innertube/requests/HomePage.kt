package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.BrowseResponse
import com.github.innertube.models.MusicTwoRowItemRenderer
import com.github.innertube.models.NavigationEndpoint
import com.github.innertube.models.bodies.BrowseBody
import com.github.innertube.utils.from
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

data class HomeSection(
    val title: String,
    val items: List<HomeItem>
)

sealed class HomeItem {
    data class SongItem(val item: Innertube.SongItem) : HomeItem()
    data class AlbumItem(val item: Innertube.AlbumItem) : HomeItem()
    data class ArtistItem(val item: Innertube.ArtistItem) : HomeItem()
    data class PlaylistItem(val item: Innertube.PlaylistItem) : HomeItem()
}

suspend fun Innertube.personalizedHome() = runCatchingNonCancellable {
    val response = client.post(BROWSE) {
        setBody(BrowseBody(browseId = "FEmusic_home"))
    }.body<BrowseResponse>()

    val sections = response.contents
        ?.singleColumnBrowseResultsRenderer
        ?.tabs
        ?.firstOrNull()
        ?.tabRenderer
        ?.content
        ?.sectionListRenderer
        ?.contents
        ?: emptyList()

    sections.mapNotNull { content ->
        val shelf = content.musicCarouselShelfRenderer ?: return@mapNotNull null
        val title = shelf.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.title
            ?.runs
            ?.firstOrNull()
            ?.text
            ?: return@mapNotNull null

        val items = shelf.contents?.mapNotNull { shelfContent ->
            shelfContent.musicTwoRowItemRenderer?.let { renderer ->
                parseTwoRowItem(renderer)
            } ?: shelfContent.musicResponsiveListItemRenderer?.let { renderer ->
                // Songs in shelves come as responsive list items
                Innertube.SongItem.from(renderer)?.let { HomeItem.SongItem(it) }
            }
        } ?: emptyList()

        if (items.isNotEmpty()) HomeSection(title = title, items = items)
        else null
    }
}

private fun parseTwoRowItem(renderer: MusicTwoRowItemRenderer): HomeItem? {
    val endpoint = renderer.navigationEndpoint?.endpoint ?: return null

    return when (endpoint) {
        is NavigationEndpoint.Endpoint.Browse -> {
            val browseId = endpoint.browseId ?: return null
            val title = renderer.title?.runs?.firstOrNull()?.text
            when {
                browseId.startsWith("VL") || browseId.startsWith("RDCLAK") ||
                browseId.startsWith("PL") || browseId.startsWith("OLAK") -> {
                    HomeItem.PlaylistItem(
                        Innertube.PlaylistItem(
                            info = Innertube.Info(
                                name = title,
                                endpoint = endpoint
                            ),
                            channel = null,
                            songCount = null,
                            thumbnail = renderer.thumbnailRenderer
                                ?.musicThumbnailRenderer
                                ?.thumbnail
                                ?.thumbnails
                                ?.firstOrNull()
                        )
                    )
                }
                browseId.startsWith("MPRE") || browseId.startsWith("UC") -> {
                    if (browseId.startsWith("UC")) {
                        HomeItem.ArtistItem(
                            Innertube.ArtistItem(
                                info = Innertube.Info(
                                    name = title,
                                    endpoint = endpoint
                                ),
                                subscribersCountText = renderer.subtitle?.runs?.map { it.text }?.joinToString(""),
                                thumbnail = renderer.thumbnailRenderer
                                    ?.musicThumbnailRenderer
                                    ?.thumbnail
                                    ?.thumbnails
                                    ?.firstOrNull()
                            )
                        )
                    } else {
                        HomeItem.AlbumItem(
                            Innertube.AlbumItem(
                                info = Innertube.Info(
                                    name = title,
                                    endpoint = endpoint
                                ),
                                authors = null,
                                year = null,
                                thumbnail = renderer.thumbnailRenderer
                                    ?.musicThumbnailRenderer
                                    ?.thumbnail
                                    ?.thumbnails
                                    ?.firstOrNull()
                            )
                        )
                    }
                }
                else -> {
                    // Default to playlist for unknown browse IDs
                    HomeItem.PlaylistItem(
                        Innertube.PlaylistItem(
                            info = Innertube.Info(
                                name = title,
                                endpoint = endpoint
                            ),
                            channel = null,
                            songCount = null,
                            thumbnail = renderer.thumbnailRenderer
                                ?.musicThumbnailRenderer
                                ?.thumbnail
                                ?.thumbnails
                                ?.firstOrNull()
                        )
                    )
                }
            }
        }
        is NavigationEndpoint.Endpoint.Watch -> {
            val title = renderer.title?.runs?.firstOrNull()?.text

            HomeItem.SongItem(
                Innertube.SongItem(
                    info = Innertube.Info(
                        name = title,
                        endpoint = endpoint
                    ),
                    authors = null,
                    album = null,
                    durationText = null,
                    thumbnail = renderer.thumbnailRenderer
                        ?.musicThumbnailRenderer
                        ?.thumbnail
                        ?.thumbnails
                        ?.firstOrNull()
                )
            )
        }
        else -> null
    }
}
