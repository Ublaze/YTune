package com.github.musicyou.ui.screens.login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.musicyou.R

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    pop: () -> Unit,
    onLoginSuccess: (cookie: String) -> Unit
) {
    val context = LocalContext.current

    val cookieManager = remember { CookieManager.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.sign_in)) },
                navigationIcon = {
                    IconButton(onClick = pop) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true

                        cookieManager.setAcceptCookie(true)
                        cookieManager.removeAllCookies(null)

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url == null) return

                                val cookies = cookieManager.getCookie("https://music.youtube.com")
                                if (cookies != null && cookies.contains("SAPISID")) {
                                    onLoginSuccess(cookies)
                                }
                            }
                        }

                        loadUrl(
                            "https://accounts.google.com/ServiceLogin" +
                                    "?service=youtube" +
                                    "&uilel=3" +
                                    "&passive=true" +
                                    "&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin" +
                                    "%3Faction_handle_signin%3Dtrue" +
                                    "%26next%3Dhttps%253A%252F%252Fmusic.youtube.com%252F"
                        )
                    }
                }
            )
        }
    }
}
