package com.withflock.flocksdk.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.withflock.flocksdk.utils.FlockEventBus
import org.json.JSONObject

internal class FlockWebViewActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URI = "extra_uri"
        var callback: FlockWebViewCallback? = null
        var backgroundColorHex: String? = null

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

        // Dynamically set background color if provided
        backgroundColorHex?.let { hex ->
            try {
                val color = hex.toColorInt()
                webView.setBackgroundColor(color)
            } catch (e: Exception) {
                // Ignore invalid color
            }
        }

        // Register navigation listener
        val navigateListener: (String, String?) -> Unit = { url, bgHex ->
            // Load the URL directly instead of sending a command
            try {
                val bgInt = bgHex?.toColorInt()
                if (bgInt != null) {
                    webView.setBackgroundColor(bgInt)
                }
                webView.loadUrl(url)
            } catch (e: Exception) {
                // Ignore invalid color
            }
        }
        FlockEventBus.registerNavigateListener(navigateListener)

        // Inject JS interface for window.ReactNativeWebView.postMessage
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun postMessage(message: String) {
                try {
                    val json = JSONObject(message)
                    val event = json.optString("event")

                    when (event) {
                        "close" -> {
                            runOnUiThread {
                                callback?.onClose()
                                callback = null
                                finish()
                            }
                        }

                        "success" -> {
                            runOnUiThread { callback?.onSuccess() }
                        }

                        "invalid" -> {
                            runOnUiThread { callback?.onInvalid() }
                        }

                        "shared" -> {
                            runOnUiThread {
                                try {
                                    // Extract the title from the data property
                                    val data = json.optJSONObject("data")
                                    val title = data?.optString("title")
                                    
                                    if (!title.isNullOrEmpty()) {
                                        // Create the share intent
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, title)
                                        }
                                        
                                        // Start the share sheet
                                        startActivity(Intent.createChooser(shareIntent, "Share via"))
                                    }
                                } catch (e: Exception) {
                                    Log.w("FlockWebViewActivity", "Error handling shared event", e)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("FlockWebViewActivity", "Error parsing message from WebView: $message", e)
                }
            }
        }, "ReactNativeWebView")

        val uri = intent.getStringExtra(EXTRA_URI) ?: ""
        webView.loadUrl(uri)

        rootLayout.addView(webView)
        setContentView(rootLayout)

        // Unregister navigation listener on destroy
        onDestroyAction = {
            FlockEventBus.unregisterNavigateListener(navigateListener)
        }
    }

    private var onDestroyAction: (() -> Unit)? = null

    override fun onDestroy() {
        onDestroyAction?.invoke()
        super.onDestroy()
    }
}
