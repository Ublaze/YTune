package com.github.musicyou.ui.screens.settings

import android.webkit.CookieManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.core.content.edit
import com.github.innertube.Innertube
import com.github.innertube.requests.accountInfo
import com.github.musicyou.R
import com.github.musicyou.utils.preferences
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.utils.ytmAccountEmailKey
import com.github.musicyou.utils.ytmAccountHandleKey
import com.github.musicyou.utils.ytmAccountNameKey
import com.github.musicyou.utils.ytmAccountPhotoKey
import com.github.musicyou.utils.ytmCookieKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccountSettings(
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var accountName by rememberPreference(ytmAccountNameKey, "")
    var accountEmail by rememberPreference(ytmAccountEmailKey, "")
    var accountPhoto by rememberPreference(ytmAccountPhotoKey, "")
    var accountHandle by rememberPreference(ytmAccountHandleKey, "")
    var cookie by rememberPreference(ytmCookieKey, "")
    var isRefreshingAccount by remember { mutableStateOf(false) }

    val isLoggedIn = cookie.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoggedIn) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile photo
            if (accountPhoto.isNotBlank()) {
                AsyncImage(
                    model = accountPhoto,
                    contentDescription = stringResource(id = R.string.account),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Account name
            if (accountName.isNotBlank()) {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Channel handle
            if (accountHandle.isNotBlank()) {
                Text(
                    text = accountHandle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Email
            if (accountEmail.isNotBlank()) {
                Text(
                    text = accountEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.connected_to_ytm),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(id = R.string.ytm_playlists_synced),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Refresh account info button
            OutlinedButton(
                onClick = {
                    if (!isRefreshingAccount) {
                        scope.launch {
                            isRefreshingAccount = true
                            withContext(Dispatchers.IO) {
                                Innertube.accountInfo()?.getOrNull()?.let { account ->
                                    context.preferences.edit {
                                        putString(ytmAccountNameKey, account.name)
                                        putString(ytmAccountEmailKey, account.email ?: "")
                                        putString(ytmAccountPhotoKey, account.photoUrl ?: "")
                                        putString(ytmAccountHandleKey, account.channelHandle ?: "")
                                    }
                                    accountName = account.name
                                    accountEmail = account.email ?: ""
                                    accountPhoto = account.photoUrl ?: ""
                                    accountHandle = account.channelHandle ?: ""
                                }
                            }
                            isRefreshingAccount = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshingAccount
            ) {
                if (isRefreshingAccount) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(text = stringResource(id = R.string.refresh_account_info))
            }

            OutlinedButton(
                onClick = {
                    cookie = ""
                    accountName = ""
                    accountEmail = ""
                    accountPhoto = ""
                    accountHandle = ""
                    Innertube.cookie = null
                    context.preferences.edit {
                        remove(ytmAccountPhotoKey)
                        remove(ytmAccountHandleKey)
                    }
                    CookieManager.getInstance().removeAllCookies(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(id = R.string.sign_out))
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = stringResource(id = R.string.not_signed_in),
                style = MaterialTheme.typography.headlineSmall
            )

            SettingsInformation(
                text = stringResource(id = R.string.sign_in_description)
            )

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Login,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = stringResource(id = R.string.sign_in))
            }
        }
    }
}
