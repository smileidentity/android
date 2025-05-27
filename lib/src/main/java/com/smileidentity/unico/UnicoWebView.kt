package com.smileidentity.unico

import android.graphics.Bitmap
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import timber.log.Timber
import androidx.core.net.toUri

@Composable
fun UnicoWebView(
    webLink: String,
    callbackUri: String,
    onProgressChanged: (Int) -> Unit,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Timber.d("UnicoWebView", "Page started loading: $url")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Timber.d("UnicoWebView", "Page finished loading: $url")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): Boolean {
                        request?.url?.let { url ->
                            val urlString = url.toString()
                            Timber.d("UnicoWebView", "URL changed to: $urlString")

                            if (urlString.startsWith(callbackUri)) {
                                handleCallback(urlString, onSuccess, onFailure)
                                return true
                            }

                            when {
                                urlString.contains("success", ignoreCase = true) ||
                                    urlString.contains("complete", ignoreCase = true) -> {
                                    onSuccess()
                                    return true
                                }

                                urlString.contains("error", ignoreCase = true) ||
                                    urlString.contains("fail", ignoreCase = true) -> {
                                    onFailure()
                                    return true
                                }

                                else -> {
                                    onFailure()
                                    return true
                                }
                            }
                        }
                        return false
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }

                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources)
                    }
                }

                loadUrl(webLink)
            }
        },
        update = { view ->
            webView = view
        },
        modifier = modifier,
    )
}

private fun handleCallback(url: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
    val uri = url.toUri()
    val status = uri.getQueryParameter("status")

    when (status) {
        "success" -> onSuccess()
        "error", "failure" -> onFailure()
    }
}
