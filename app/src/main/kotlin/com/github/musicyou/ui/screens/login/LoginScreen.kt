package com.github.musicyou.ui.screens.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
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
import com.github.musicyou.R

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
                        settings.domStorageEnabled = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

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
                            }

                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean
                            ) {
                                if (loginHandled) return
                                if (url?.startsWith("https://music.youtube.com") == true) {
                                    cookieManager.flush()
                                    checkForLoginCookies(cookieManager)?.let { cookies ->
                                        loginHandled = true
                                        onLoginSuccess(cookies)
                                    }
                                }
                            }
                        }

                        // Load YouTube Music directly — it will redirect to
                        // Google sign-in if needed, and uses the simpler
                        // account picker when accounts exist on the device
                        loadUrl("https://music.youtube.com")
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

private fun checkForLoginCookies(cookieManager: CookieManager): String? {
    // Check multiple domains where Google sets auth cookies
    val domains = listOf(
        "https://music.youtube.com",
        "https://www.youtube.com",
        "https://.youtube.com"
    )

    for (domain in domains) {
        val cookies = cookieManager.getCookie(domain)
        if (cookies != null && (cookies.contains("SAPISID") || cookies.contains("__Secure-3PAPISID"))) {
            // Get the full cookie string from music.youtube.com specifically
            // as that's what the Innertube API needs
            val musicCookies = cookieManager.getCookie("https://music.youtube.com")
            if (musicCookies != null && musicCookies.contains("SAPISID")) {
                return musicCookies
            }
            // Fall back to the domain where we found cookies
            return cookies
        }
    }
    return null
}
