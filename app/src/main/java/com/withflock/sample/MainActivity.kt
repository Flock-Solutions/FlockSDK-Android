package com.withflock.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.withflock.flocksdk.FlockSDK
import com.withflock.flocksdk.FlockEnvironment
import com.withflock.flocksdk.ui.FlockWebViewCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FlockSDK.initialize(
            publicAccessKey = "pk_ba3b841f41c4b26cc34fa6aebf660efb",
            environment = FlockEnvironment.TEST
        )

        findViewById<Button>(R.id.referrerButton).setOnClickListener {
            FlockSDK.openPage(this, "referrer")
        }
        findViewById<Button>(R.id.inviteeButton).setOnClickListener {
            FlockSDK.openPage(this, "invitee", object : FlockWebViewCallback {
                override fun onClose() {}
                override fun onSuccess() {
                    FlockSDK.navigate("invitee?state=success")
                }
                override fun onInvalid() {}
            })
        }
    }
}
