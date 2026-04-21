package com.github.musicyou.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.innertube.Innertube
import com.github.innertube.requests.libraryPlaylists
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
import com.github.musicyou.utils.playlistSortByKey
import com.github.musicyou.utils.playlistSortOrderKey
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.viewmodels.HomePlaylistsViewModel
import kotlinx.coroutines.launch

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
    val playerPadding = LocalPlayerPadding.current
    val scope = rememberCoroutineScope()

    var isCreatingANewPlaylist by rememberSaveable { mutableStateOf(false) }
    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.Name)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Ascending)

    var ytmPlaylists by remember { mutableStateOf<List<Innertube.PlaylistItem>>(emptyList()) }
    var isLoadingYtmPlaylists by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val viewModel: HomePlaylistsViewModel = viewModel()

    LaunchedEffect(sortBy, sortOrder) {
        viewModel.loadArtists(
            sortBy = sortBy,
            sortOrder = sortOrder
        )
    }

    LaunchedEffect(Innertube.isLoggedIn) {
        if (Innertube.isLoggedIn) {
            isLoadingYtmPlaylists = true
            Innertube.libraryPlaylists()?.getOrNull()?.let {
                ytmPlaylists = it
            }
            isLoadingYtmPlaylists = false
        } else {
            ytmPlaylists = emptyList()
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
            isRefreshing = isRefreshing,
            onRefresh = {
                if (!isRefreshing) {
                    scope.launch {
                        isRefreshing = true
                        if (Innertube.isLoggedIn) {
                            isLoadingYtmPlaylists = true
                            Innertube.libraryPlaylists()?.getOrNull()?.let {
                                ytmPlaylists = it
                            }
                            isLoadingYtmPlaylists = false
                        }
                        isRefreshing = false
                    }
                }
            },
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

                if (Innertube.isLoggedIn && isLoadingYtmPlaylists) {
                    item(
                        key = "ytm_loading",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ShimmerHost {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(3) {
                                    ItemPlaceholder(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else if (ytmPlaylists.isNotEmpty()) {
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

                    items(
                        items = ytmPlaylists,
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
                } else if (!Innertube.isLoggedIn) {
                    item(
                        key = "ytm_signin_cta",
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .clickable(onClick = onLoginClick),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.ytm_sign_in_playlists_prompt),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(id = R.string.sign_in),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(
                    items = viewModel.items,
                    key = { it.playlist.id }
                ) { playlistPreview ->
                    LocalPlaylistItem(
                        modifier = Modifier.animateItem(),
                        playlist = playlistPreview,
                        isSynced = playlistPreview.browseId != null,
                        onClick = { onPlaylistClick(playlistPreview.playlist) }
                    )
                }
            }
        }
    }
}