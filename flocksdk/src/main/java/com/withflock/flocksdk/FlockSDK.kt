package com.withflock.flocksdk

import android.content.Context
import android.util.Log
import com.withflock.flocksdk.model.Campaign
import com.withflock.flocksdk.model.Customer
import com.withflock.flocksdk.model.IdentifyRequest
import com.withflock.flocksdk.network.CampaignService
import com.withflock.flocksdk.network.CustomerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FlockSDK {
    private const val APP_BASE_URL = "https://app.withflock.com"

    private lateinit var environment: FlockEnvironment
    private lateinit var publicAccessKey: String
    private var campaign: Campaign? = null
    private var customer: Customer? = null

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
        if (!::publicAccessKey.isInitialized) {
            throw IllegalStateException("FlockSDK not initialized")
        }

        val request = IdentifyRequest(
            externalUserId = externalUserId,
            email = email,
            name = name
        )

        return withContext(Dispatchers.IO) {
            val service = CustomerService(publicAccessKey)
            val result = service.identify(request)
            customer = result
            result
        }
    }

    /**
     * Opens a Flock webpage in a full-screen modal.
     *
     * @param context The current Activity context
     * @param pageType The page type to open (e.g., "referrer", "invitee")
     */
    fun openWebPage(context: Context, pageType: String) {
        val path = pageType.split("?").first()
        val query = pageType.split("?").getOrElse(1) { "" }
        val campaignPage = campaign?.campaignPages?.find { it.path.contains(pageType) }
        val url = "$APP_BASE_URL/pages/$path?key=$publicAccessKey&campaign_id=${campaign?.id}&customer_id=${customer?.id}&bg=${campaignPage?.screenProps?.backgroundColor}&$query"

        FlockWebViewActivity.start(context, url)
    }

    private fun fetchLiveCampaign() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val service = CampaignService(publicAccessKey)
                val result = service.getLiveCampaign()
                campaign = result
                Log.d("FlockSDK", "Fetched campaign: ${result.id}")
            } catch (e: Exception) {
                Log.e("FlockSDK", "Failed to fetch campaign: ${e.message}", e)
            }
        }
    }
}