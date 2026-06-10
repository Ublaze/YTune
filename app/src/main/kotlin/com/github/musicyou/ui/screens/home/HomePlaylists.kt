package com.github.musicyou.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.innertube.Innertube
import com.github.innertube.requests.libraryPlaylists
import com.github.innertube.utils.isUnauthorized
import com.github.musicyou.LocalPlayerPadding
import com.github.musicyou.R
import com.github.musicyou.database
import com.github.musicyou.enums.BuiltInPlaylist
import com.github.musicyou.enums.PlaylistSortBy
import com.github.musicyou.enums.SortOrder
import com.github.musicyou.models.Playlist
import com.github.musicyou.ui.components.HomeScaffold
import com.github.musicyou.ui.components.ShimmerHost
import com.github.musicyou.ui.components.SortingHeader
import com.github.musicyou.ui.components.TextFieldDialog
import com.github.musicyou.ui.items.BuiltInPlaylistItem
import com.github.musicyou.ui.items.ItemPlaceholder
import com.github.musicyou.ui.items.LocalPlaylistItem
import com.github.musicyou.ui.items.PlaylistItem
import com.github.musicyou.utils.loadYtmPlaylistsCache
import com.github.musicyou.utils.playlistSortByKey
import com.github.musicyou.utils.playlistSortOrderKey
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.utils.saveYtmPlaylistsCache
import com.github.musicyou.utils.ytmCookieKey
import com.github.musicyou.viewmodels.HomePlaylistsViewModel

// YouTube Music's built-in "Liked Songs" playlist.
private const val LIKED_SONGS_BROWSE_ID = "VLLM"

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun HomePlaylists(
    openSearch: () -> Unit,
    openSettings: () -> Unit,
    onBuiltInPlaylist: (Int) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onYtmPlaylistClick: (String) -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val playerPadding = LocalPlayerPadding.current

    var isCreatingANewPlaylist by rememberSaveable { mutableStateOf(false) }
    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.Name)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Ascending)

    val cookie by rememberPreference(ytmCookieKey, "")
    val isLoggedIn = cookie.isNotBlank()

    var ytmPlaylists by remember { mutableStateOf<List<Innertube.PlaylistItem>>(emptyList()) }
    var isLoadingYtmPlaylists by remember { mutableStateOf(false) }
    var isYtmSessionExpired by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val viewModel: HomePlaylistsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadArtists(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    LaunchedEffect(isLoggedIn, refreshTrigger) {
        if (isLoggedIn) {
            // Show the cached playlists instantly while the live fetch runs.
            if (ytmPlaylists.isEmpty()) {
                ytmPlaylists = context.loadYtmPlaylistsCache()
            }

            isLoadingYtmPlaylists = true
            val result = Innertube.libraryPlaylists()
            isLoadingYtmPlaylists = false

            result?.onSuccess { playlists ->
                ytmPlaylists = playlists
                isYtmSessionExpired = false
                context.saveYtmPlaylistsCache(playlists)
            }

            result?.onFailure { error ->
                if (error.isUnauthorized) isYtmSessionExpired = true
            }
        } else {
            ytmPlaylists = emptyList()
            isYtmSessionExpired = false
        }
    }

    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            title = stringResource(id = R.string.new_playlist),
            hintText = stringResource(id = R.string.playlist_name_hint),
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                database.query {
                    database.insert(Playlist(name = text))
                }
            }
        )
    }

    HomeScaffold(
        title = R.string.playlists,
        openSearch = openSearch,
        openSettings = openSettings
    ) {
        PullToRefreshBox(
            isRefreshing = isLoadingYtmPlaylists && isLoggedIn,
            onRefresh = { refreshTrigger++ },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 16.dp + playerPadding
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(
                    key = "header",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    SortingHeader(
                        sortBy = sortBy,
                        changeSortBy = { sortBy = it },
                        sortByEntries = PlaylistSortBy.entries.toList(),
                        sortOrder = sortOrder,
                        toggleSortOrder = { sortOrder = !sortOrder },
                        size = viewModel.items.size,
                        itemCountText = R.plurals.number_of_playlists
                    )
                }

                item(key = "favorites") {
                    BuiltInPlaylistItem(
                        icon = Icons.Default.Favorite,
                        name = stringResource(id = R.string.favorites),
                        onClick = { onBuiltInPlaylist(BuiltInPlaylist.Favorites.ordinal) }
                    )
                }

                item(key = "offline") {
                    BuiltInPlaylistItem(
                        icon = Icons.Default.DownloadForOffline,
                        name = stringResource(id = R.string.offline),
                        onClick = { onBuiltInPlaylist(BuiltInPlaylist.Offline.ordinal) }
                    )
                }

                item(key = "new") {
                    BuiltInPlaylistItem(
                        icon = Icons.Default.Add,
                        name = stringResource(id = R.string.new_playlist),
                        onClick = { isCreatingANewPlaylist = true }
                    )
                }

                if (isLoggedIn) {
                    item(
                        key = "ytm_header",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.youtube_music_playlists),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(
                                start = 8.dp,
                                top = 16.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    if (isYtmSessionExpired) {
                        item(
                            key = "ytm_session_expired",
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.session_expired),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )

                                    FilledTonalButton(onClick = onLoginClick) {
                                        Text(text = stringResource(id = R.string.sign_in))
                                    }
                                }
                            }
                        }
                    }

                    item(key = "ytm_liked") {
                        BuiltInPlaylistItem(
                            icon = Icons.Default.Favorite,
                            name = stringResource(id = R.string.liked_songs),
                            onClick = { onYtmPlaylistClick(LIKED_SONGS_BROWSE_ID) }
                        )
                    }

                    if (ytmPlaylists.isEmpty() && isLoadingYtmPlaylists) {
                        items(
                            count = 3,
                            key = { "ytm_placeholder_$it" }
                        ) {
                            ShimmerHost {
                                ItemPlaceholder()
                            }
                        }
                    }

                    items(
                        // Liked Songs is pinned above; drop it if the library lists it too.
                        items = ytmPlaylists.filter { it.key != LIKED_SONGS_BROWSE_ID },
                        key = { "ytm_${it.key}" }
                    ) { playlist ->
                        PlaylistItem(
                            modifier = Modifier.animateItem(),
                            playlist = playlist,
                            onClick = {
                                playlist.info?.endpoint?.browseId?.let { browseId ->
                                    onYtmPlaylistClick(browseId)
                                }
                            }
                        )
                    }
                }

                items(
                    items = viewModel.items,
                    key = { it.playlist.id }
                ) { playlistPreview ->
                    LocalPlaylistItem(
                        modifier = Modifier.animateItem(),
                        playlist = playlistPreview,
                        onClick = { onPlaylistClick(playlistPreview.playlist) }
                    )
                }
            }
        }
    }
}
