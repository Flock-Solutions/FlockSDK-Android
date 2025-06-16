package com.withflock.flocksdk

import android.content.Context
import android.util.Log
import com.withflock.flocksdk.model.Campaign
import com.withflock.flocksdk.model.Customer
import com.withflock.flocksdk.model.IdentifyRequest
import com.withflock.flocksdk.network.CampaignService
import com.withflock.flocksdk.network.CustomerService
import com.withflock.flocksdk.ui.FlockWebViewActivity
import com.withflock.flocksdk.ui.FlockWebViewCallback
import com.withflock.flocksdk.utils.FlockEventBus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FlockSDK {
    private var uiBaseUrl: String = "https://app.withflock.com"
    private var apiBaseUrl: String = "https://api.withflock.com"

    private lateinit var environment: FlockEnvironment
    private lateinit var publicAccessKey: String
    private var campaign: Campaign? = null
    private var customer: Customer? = null
    private var isInitialized = false
    private var isIdentified = false
    private val initalizationCompletionQueue = mutableListOf<() -> Unit>()
    private val identifyCompletionQueue = mutableListOf<() -> Unit>()

    /**
     * For internal/testing use only: allows overriding the base URL (e.g. in a sample app)
     */
    fun setBaseUrlForTesting(uiUrl: String, apiUrl: String) {
        uiBaseUrl = uiUrl
        apiBaseUrl = apiUrl
    }

    /**
     * Initializes the Flock SDK.
     *
     * @param environment Either FlockEnvironment.TEST or FlockEnvironment.PRODUCTION
     * @param publicAccessKey The public access key for your campaign
     */
    fun initialize(
        publicAccessKey: String,
        environment: FlockEnvironment
    ) {
        this.publicAccessKey = publicAccessKey
        this.environment = environment

        fetchLiveCampaign()
    }

    /**
     * Returns true if the SDK has been fully initialized & campaign has been fetched successfully
     */
    fun isInitialized(): Boolean = campaign != null && isInitialized

    fun getCurrentCampaign(): Campaign? = campaign

    /**
     * Identifies a customer to Flock.
     *
     * @param externalUserId An opaque identifier for the user in your app
     * @param email The user's email address
     * @param name The user's name
     * @return The identified customer
     */
    suspend fun identify(
        externalUserId: String,
        email: String,
        name: String,
    ): Customer {
        if (!isInitialized) {
            // Queue identify until initialization is complete
            val deferred = CompletableDeferred<Customer>()
            initalizationCompletionQueue.add {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        deferred.complete(identify(externalUserId, email, name))
                    } catch (e: Exception) {
                        deferred.completeExceptionally(e)
                    }
                }
            }
            return deferred.await()
        }

        val request = IdentifyRequest(
            externalUserId = externalUserId,
            email = email,
            name = name
        )

        return withContext(Dispatchers.IO) {
            val service = CustomerService(publicAccessKey, apiBaseUrl)
            val result = service.identify(request)
            customer = result
            isIdentified = true
            processIdentifyCompletionQueue()
            result
        }
    }

    /**
     * Opens a Flock webpage in a full-screen modal.
     *
     * @deprecated Use addPlacement instead.
     */
    @Deprecated("Use addPlacement instead.")
    fun openPage(context: Context, pageType: String, callback: FlockWebViewCallback? = null) {
        if (!isInitialized || !isIdentified) {
            identifyCompletionQueue.add {
                openPage(context, pageType, callback)
            }
            return
        }

        val path = pageType.split("?").first()
        val query = pageType.split("?").getOrElse(1) { "" }
        val campaignPage = campaign?.campaignPages?.find { it.path.contains(pageType) }
        val backgroundColor = campaignPage?.screenProps?.backgroundColor
        val url =
            "$uiBaseUrl/pages/$path?key=$publicAccessKey&campaign_id=${campaign?.id}&customer_id=${customer?.id}&bg=$backgroundColor&$query"

        FlockWebViewActivity.callback = callback
        FlockWebViewActivity.backgroundColorHex = backgroundColor
        FlockWebViewActivity.start(context, url)
    }

    /**
     * Opens a Flock placement in a full-screen modal.
     *
     * @param context The current Activity context
     * @param placementId The placement ID to open
     * @param callback Optional callback to react to web events (close, success, invalid)
     *
     * Example usage:
     * ```kotlin
     * FlockSDK.addPlacement(context, "your_placement_id", object : FlockWebViewCallback { ... })
     * ```
     */
    fun addPlacement(context: Context, placementId: String, callback: FlockWebViewCallback? = null) {
        if (!isInitialized || !isIdentified) {
            identifyCompletionQueue.add {
                addPlacement(context, placementId, callback)
            }
            return
        }

        // Try to get background color for placement if available (fallback to null)
        val campaignPlacement = campaign?.campaignPages?.find { it.placementId == placementId }
        val backgroundColor = campaignPlacement?.screenProps?.backgroundColor
        val url = buildString {
            append("$uiBaseUrl/placements/$placementId?key=$publicAccessKey&campaign_id=${campaign?.id}&customer_id=${customer?.id}")
            if (backgroundColor != null) {
                append("&bg=${android.net.Uri.encode(backgroundColor)}")
            }
        }

        FlockWebViewActivity.callback = callback
        FlockWebViewActivity.backgroundColorHex = backgroundColor
        FlockWebViewActivity.start(context, url)
    }

    /**
     * Navigates to a page within the same web view.
     *
     * @param pageType The pageType to navigate to.
     *
     * Example usage:
     * ```kotlin
     * FlockSDK.navigate("invitee?state=success")
     * ```
     */
    fun navigate(pageType: String) {
        FlockEventBus.postNavigate(pageType)
    }

    private fun processInitalizationCompletionQueue() {
        val queueCopy = initalizationCompletionQueue.toList()
        initalizationCompletionQueue.clear()
        queueCopy.forEach { it() }
    }

    private fun processIdentifyCompletionQueue() {
        val queueCopy = identifyCompletionQueue.toList()
        identifyCompletionQueue.clear()
        queueCopy.forEach { it() }
    }

    private fun fetchLiveCampaign() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val service = CampaignService(publicAccessKey, apiBaseUrl)
                val result = service.getLiveCampaign(environment)
                campaign = result
                Log.d("FlockSDK", "Fetched campaign: ${result.id}")
                try {
                    service.ping(result.id)
                } catch (e: Exception) {
                    Log.e("FlockSDK", "Ping failed", e)
                }
                isInitialized = true
                processInitalizationCompletionQueue()
            } catch (e: Exception) {
                Log.e("FlockSDK", "Failed to fetch campaign: ${e.message}", e)
            }
        }
    }
}