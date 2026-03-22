package com.github.musicyou.ui.screens.settings

import android.webkit.CookieManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.innertube.Innertube
import com.github.musicyou.R
import com.github.musicyou.utils.preferences
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.utils.ytmAccountEmailKey
import com.github.musicyou.utils.ytmAccountNameKey
import com.github.musicyou.utils.ytmCookieKey
import androidx.core.content.edit

@Composable
fun AccountSettings(
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var accountName by rememberPreference(ytmAccountNameKey, "")
    var accountEmail by rememberPreference(ytmAccountEmailKey, "")
    var cookie by rememberPreference(ytmCookieKey, "")

    val isLoggedIn = cookie.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        if (isLoggedIn) {
            if (accountName.isNotBlank()) {
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (accountEmail.isNotBlank()) {
                Text(
                    text = accountEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsInformation(
                text = stringResource(id = R.string.signed_in_description)
            )

            OutlinedButton(
                onClick = {
                    cookie = ""
                    accountName = ""
                    accountEmail = ""
                    Innertube.cookie = null
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
