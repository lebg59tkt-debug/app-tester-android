package com.testshare.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
        }
        web.addJavascriptInterface(Bridge(), "Android")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest): Boolean {
                val url = req.url.toString()
                if (url.startsWith("file://")) return false
                openExternal(url)
                return true
            }
        }
        if (savedInstanceState != null) web.restoreState(savedInstanceState)
        else web.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("window.handleBack && window.handleBack()") { r ->
                    if (r != "true") { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
                }
            }
        })
    }

    private fun openExternal(url: String) {
        if (!url.startsWith("https://")) return
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    inner class Bridge {
        @JavascriptInterface
        fun openExternal(url: String) = runOnUiThread { this@MainActivity.openExternal(url) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }
}
