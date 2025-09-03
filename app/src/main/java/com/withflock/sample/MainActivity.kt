package com.withflock.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.withflock.flocksdk.FlockEnvironment
import com.withflock.flocksdk.FlockSDK
import com.withflock.flocksdk.ui.FlockWebViewCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FlockSDK.initialize(
            publicAccessKey = "pk_ba3b841f41c4b26cc34fa6aebf660efb",
            environment = FlockEnvironment.TEST
        )

        lifecycleScope.launch {
            FlockSDK.identify(
                externalUserId = "user_123",
                email = "jane@example.com",
                name = "Jane Doe"
            )
        }

        findViewById<Button>(R.id.referrerButton).setOnClickListener {
            FlockSDK.checkpoint("cta_button_clicked").trigger(this)
        }
        findViewById<Button>(R.id.inviteeButton).setOnClickListener {
            FlockSDK.checkpoint("user_signup").onSuccess {
                FlockSDK.checkpoint("referral_succeeded").navigate().trigger(this)
            }.trigger(this)
        }
    }
}
