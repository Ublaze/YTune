package com.github.musicyou.ui.screens.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.innertube.Innertube
import com.github.musicyou.R

private const val GOOGLE_LOGIN_URL =
    "https://accounts.google.com/ServiceLogin" +
    "?ltmpl=music" +
    "&service=youtube" +
    "&passive=true" +
    "&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin" +
    "%3Faction_handle_signin%3Dtrue" +
    "%26next%3Dhttps%253A%252F%252Fmusic.youtube.com%252F"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    pop: () -> Unit,
    onLoginSuccess: (cookie: String) -> Unit
) {
    val cookieManager = remember { CookieManager.getInstance() }
    var isLoading by remember { mutableStateOf(true) }
    var loginHandled by remember { mutableStateOf(false) }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true

                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // JavaScript bridge to extract visitorData from YTM page
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onRetrieveVisitorData(newVisitorData: String?) {
                                if (newVisitorData != null) {
                                    Innertube.visitorData = newVisitorData
                                }
                            }
                        }, "Android")

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = false

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                // Try to extract visitorData from YouTube Music page
                                if (url?.contains("music.youtube.com") == true) {
                                    view?.loadUrl(
                                        "javascript:Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA)"
                                    )
                                }
                            }

                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean
                            ) {
                                if (loginHandled) return
                                // After Google login, the redirect chain ends at music.youtube.com
                                if (url?.startsWith("https://music.youtube.com") == true) {
                                    cookieManager.flush()
                                    val cookies = cookieManager.getCookie(url)
                                    if (cookies != null && cookies.contains("SAPISID")) {
                                        loginHandled = true
                                        onLoginSuccess(cookies)
                                    }
                                }
                            }
                        }

                        // Load Google ServiceLogin — redirects to music.youtube.com after login
                        loadUrl(GOOGLE_LOGIN_URL)
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
