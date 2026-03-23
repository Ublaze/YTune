package com.github.musicyou.ui.screens.settings

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.github.api.GitHub
import com.github.musicyou.LocalPlayerPadding
import com.github.musicyou.R
import com.github.musicyou.ui.styling.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private fun compareVersions(v1: String, v2: String): Int {
    val parts1 = v1.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = v2.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(parts1.size, parts2.size)) {
        val p1 = parts1.getOrElse(i) { 0 }
        val p2 = parts2.getOrElse(i) { 0 }
        if (p1 != p2) return p1 - p2
    }
    return 0
}

@ExperimentalAnimationApi
@Composable
fun About() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val playerPadding = LocalPlayerPadding.current
    val scope = rememberCoroutineScope()

    var isShowingDialog by remember { mutableStateOf(false) }
    var latestVersion: String? by rememberSaveable { mutableStateOf(null) }
    var newVersionAvailable: Boolean? by rememberSaveable { mutableStateOf(null) }
    var apkDownloadUrl: String? by rememberSaveable { mutableStateOf(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadError: String? by remember { mutableStateOf(null) }

    val currentVersion =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 16.dp + playerPadding)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(125.dp)
                .aspectRatio(1F),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "${stringResource(id = R.string.app_name)} v$currentVersion",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                // Reset state for fresh check
                latestVersion = null
                newVersionAvailable = null
                apkDownloadUrl = null
                downloadError = null
                isDownloading = false
                downloadProgress = 0f
                isShowingDialog = true
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Outlined.Update,
                contentDescription = stringResource(id = R.string.check_for_updates)
            )

            Spacer(Modifier.size(ButtonDefaults.IconSpacing))

            Text(text = stringResource(id = R.string.check_for_updates))
        }

        Spacer(modifier = Modifier.height(Dimensions.spacer + 8.dp))

        ListItem(
            headlineContent = {
                Text(text = stringResource(id = R.string.github))
            },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = stringResource(id = R.string.github)
                )
            },
            modifier = Modifier.clickable {
                uriHandler.openUri("https://github.com/Ublaze/YTune")
            }
        )
    }

    if (isShowingDialog) {
        LaunchedEffect(Unit) {
            val release = GitHub.getLastestRelease()
            latestVersion = release?.name
            apkDownloadUrl = release?.apkDownloadUrl
            latestVersion?.let {
                newVersionAvailable = compareVersions(it, currentVersion) > 0
            }
        }

        AlertDialog(
            onDismissRequest = {
                if (!isDownloading) isShowingDialog = false
            },
            confirmButton = {
                if (!isDownloading) {
                    TextButton(
                        onClick = { isShowingDialog = false }
                    ) {
                        Text(text = stringResource(id = R.string.close))
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(
                        id = when {
                            isDownloading -> R.string.downloading_update
                            newVersionAvailable == true -> R.string.new_version_available
                            newVersionAvailable == false -> R.string.no_updates_available
                            else -> R.string.checking_for_updates
                        }
                    )
                )
            },
            text = {
                when {
                    isDownloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    newVersionAvailable == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    newVersionAvailable == true -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.version,
                                    latestVersion ?: ""
                                ),
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (downloadError != null) {
                                Text(
                                    text = downloadError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            if (apkDownloadUrl != null) {
                                FilledTonalButton(
                                    onClick = {
                                        scope.launch {
                                            isDownloading = true
                                            downloadProgress = 0f
                                            downloadError = null
                                            try {
                                                val apkFile = withContext(Dispatchers.IO) {
                                                    downloadApk(
                                                        url = apkDownloadUrl!!,
                                                        cacheDir = context.cacheDir,
                                                        onProgress = { downloadProgress = it }
                                                    )
                                                }
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    apkFile
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(
                                                        uri,
                                                        "application/vnd.android.package-archive"
                                                    )
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                                isShowingDialog = false
                                            } catch (e: Exception) {
                                                downloadError = "Download failed: ${e.message}"
                                            } finally {
                                                isDownloading = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Download,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(text = stringResource(id = R.string.download_and_install))
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    uriHandler.openUri("https://github.com/Ublaze/YTune/releases/latest")
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.github),
                                    contentDescription = stringResource(id = R.string.github)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(text = stringResource(id = R.string.open_in_github))
                            }
                        }
                    }
                }
            }
        )
    }
}

private fun downloadApk(url: String, cacheDir: File, onProgress: (Float) -> Unit): File {
    val file = File(cacheDir, "YTune-update.apk")
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = true
    connection.connect()

    val contentLength = connection.contentLength.toLong()
    connection.inputStream.use { input ->
        file.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var downloaded = 0L
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
                downloaded += read
                if (contentLength > 0) onProgress(downloaded.toFloat() / contentLength)
            }
        }
    }
    onProgress(1f)
    return file
}
