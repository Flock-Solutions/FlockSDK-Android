package com.withflock.flocksdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class FlockWebViewActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URI = "extra_uri"

        fun start(context: Context, uri: String) {
            val intent = Intent(context, FlockWebViewActivity::class.java)
            intent.putExtra(EXTRA_URI, uri)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Create layout programmatically
        val rootLayout = FrameLayout(this)
        rootLayout.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        val webView = WebView(this)
        webView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // Inject JS interface for window.ReactNativeWebView.postMessage
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun postMessage(message: String) {
                try {
                    val json = JSONObject(message)
                    if (json.optString("event") == "Close") {
                        runOnUiThread { finish() }
                    }
                } catch (e: Exception) {
                    // Optionally log or handle parse error
                }
            }
        }, "ReactNativeWebView")

        val uri = intent.getStringExtra(EXTRA_URI) ?: ""
        webView.loadUrl(uri)

        rootLayout.addView(webView)
        setContentView(rootLayout)
    }
}
