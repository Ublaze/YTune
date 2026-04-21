package com.github.musicyou.ui.screens.localplaylist

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.innertube.Innertube
import com.github.innertube.requests.playlistPage
import com.github.musicyou.R
import com.github.musicyou.database
import com.github.musicyou.models.Playlist
import com.github.musicyou.models.SongPlaylistMap
import com.github.musicyou.ui.components.ConfirmationDialog
import com.github.musicyou.ui.components.TextFieldDialog
import com.github.musicyou.ui.components.TooltipIconButton
import com.github.musicyou.utils.asMediaItem
import com.github.musicyou.utils.completed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun LocalPlaylistScreen(
    playlistId: Long,
    pop: () -> Unit,
    onGoToAlbum: (String) -> Unit,
    onGoToArtist: (String) -> Unit
) {
    var playlist: Playlist? by remember { mutableStateOf(null) }

    var isRenaming by rememberSaveable { mutableStateOf(false) }
    var isDeleting by rememberSaveable { mutableStateOf(false) }
    var isSyncing by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    val syncedMessage = stringResource(id = R.string.playlist_synced)
    val syncFailedMessage = stringResource(id = R.string.playlist_sync_failed)

    LaunchedEffect(Unit) {
        database.playlist(playlistId).filterNotNull().collect { playlist = it }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (!playlist?.browseId.isNullOrEmpty()) {
                        if (isSyncing) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            TooltipIconButton(
                                description = R.string.sync_playlist,
                                onClick = {
                                    playlist?.browseId?.let { browseId ->
                                        coroutineScope.launch {
                                            isSyncing = true
                                            var syncSucceeded = false
                                            try {
                                                val remotePlaylist = withContext(Dispatchers.IO) {
                                                    Innertube.playlistPage(browseId = browseId)
                                                        ?.completed()
                                                        ?.getOrNull()
                                                }
                                                if (remotePlaylist != null) {
                                                    database.transaction {
                                                        database.clearPlaylist(playlistId)

                                                        remotePlaylist.songsPage
                                                            ?.items
                                                            ?.map(Innertube.SongItem::asMediaItem)
                                                            ?.onEach(database::insert)
                                                            ?.mapIndexed { position, mediaItem ->
                                                                SongPlaylistMap(
                                                                    songId = mediaItem.mediaId,
                                                                    playlistId = playlistId,
                                                                    position = position
                                                                )
                                                            }?.let(database::insertSongPlaylistMaps)
                                                    }
                                                    syncSucceeded = true
                                                }
                                            } finally {
                                                isSyncing = false
                                                snackbarHostState.showSnackbar(
                                                    if (syncSucceeded) syncedMessage else syncFailedMessage
                                                )
                                            }
                                        }
                                    }
                                },
                                icon = Icons.Outlined.Sync,
                                inTopBar = true
                            )
                        }
                    }

                    TooltipIconButton(
                        description = R.string.rename_playlist,
                        onClick = { isRenaming = true },
                        icon = Icons.Outlined.Edit,
                        inTopBar = true
                    )

                    TooltipIconButton(
                        description = R.string.delete_playlist,
                        onClick = { isDeleting = true },
                        icon = Icons.Outlined.Delete,
                        inTopBar = true
                    )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LocalPlaylistSongs(
                playlistId = playlistId,
                onGoToAlbum = onGoToAlbum,
                onGoToArtist = onGoToArtist
            )

            if (isRenaming) {
                TextFieldDialog(
                    title = stringResource(id = R.string.rename_playlist),
                    hintText = stringResource(id = R.string.playlist_name_hint),
                    initialTextInput = playlist?.name ?: "",
                    onDismiss = { isRenaming = false },
                    onDone = { text ->
                        database.query {
                            playlist?.copy(name = text)
                                ?.let(database::update)
                        }
                    }
                )
            }

            if (isDeleting) {
                ConfirmationDialog(
                    title = stringResource(id = R.string.delete_playlist_dialog),
                    onDismiss = { isDeleting = false },
                    onConfirm = {
                        database.query {
                            playlist?.let(database::delete)
                        }
                        pop()
                    }
                )
            }
        }
    }
}
