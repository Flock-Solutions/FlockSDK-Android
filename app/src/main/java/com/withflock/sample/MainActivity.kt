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

        FlockSDK.setBaseUrlForTesting(
            uiUrl = "https://app-dev.withflock.com",
            apiUrl = "https://api-dev.withflock.com"
        )

        FlockSDK.initialize(
            publicAccessKey = "pk_b417b6bfb718a8faef4e52b8336cc8b4",
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
            FlockSDK.addPlacement(this, "referrer")
        }
        findViewById<Button>(R.id.inviteeButton).setOnClickListener {
            FlockSDK.addPlacement(this, "invitee", object : FlockWebViewCallback {
                override fun onClose() {}
                override fun onSuccess() {
                    FlockSDK.navigate("invitee_success")
                }

                override fun onInvalid() {}
            })
        }
    }
}
