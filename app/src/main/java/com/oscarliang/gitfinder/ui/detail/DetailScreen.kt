package com.oscarliang.gitfinder.ui.detail

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Base64

@Composable
fun DetailScreen(repoUrl: String?) {
    AndroidView(
        factory = { context ->
            WebView(context)
        },
        update = { webView ->
            if (repoUrl != null) {
                val url = String(Base64.getUrlDecoder().decode(repoUrl.toString()))
                webView.loadUrl(url)
            }
        }
    )
}