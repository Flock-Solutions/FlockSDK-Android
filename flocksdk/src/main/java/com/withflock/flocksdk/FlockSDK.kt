package com.withflock.flocksdk

import android.content.Context
import android.net.Uri
import android.util.Log
import com.withflock.flocksdk.model.Campaign
import com.withflock.flocksdk.model.CampaignPage
import com.withflock.flocksdk.model.Customer
import com.withflock.flocksdk.model.IdentifyRequest
import com.withflock.flocksdk.network.CampaignService
import com.withflock.flocksdk.network.CustomerService
import com.withflock.flocksdk.ui.FlockWebViewActivity
import com.withflock.flocksdk.ui.FlockWebViewCallback
import com.withflock.flocksdk.utils.FlockEventBus
import kotlinx.coroutines.Dispatchers
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
    private val identifyCompletionQueue = mutableListOf<() -> Unit>()

    // Logging configuration
    private const val TAG = "FlockSDK"
    private var loggingEnabled = false
    private var logLevel = LogLevel.INFO

    /**
     * Log levels for the SDK
     */
    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR, NONE
    }

    /**
     * For internal/testing use only: allows overriding the base URL (e.g. in a sample app)
     */
    fun setBaseUrlForTesting(uiUrl: String, apiUrl: String) {
        uiBaseUrl = uiUrl
        apiBaseUrl = apiUrl
        logDebug("Base URLs set to $uiUrl and $apiUrl")
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
        if (publicAccessKey.isBlank()) {
            logError("Failed to initialize: publicAccessKey cannot be empty")
            return
        }

        this.publicAccessKey = publicAccessKey
        this.environment = environment
        isInitialized = true
    }

    /**
     * Returns true if the SDK has been fully initialized & campaign has been fetched successfully
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Returns true if a user has been identified
     */
    fun isIdentified(): Boolean = isIdentified

    /**
     * Returns the current campaign if available
     */
    fun getCurrentCampaign(): Campaign? = campaign

    /**
     * Returns the current customer if available
     */
    fun getCurrentCustomer(): Customer? = customer

    /**
     * Clears the current user session
     * This will reset the identified state and clear any user-specific data
     */
    fun clearSession() {
        customer = null
        isIdentified = false
        logInfo("User session cleared")
    }

    /**
     * Identifies a customer to Flock.
     *
     * @param externalUserId An opaque identifier for the user in your app
     * @param email The user's email address
     * @param name The user's name
     * @param customProperties Optional custom properties for the user
     * @return The identified customer or null if identification failed
     */
    suspend fun identify(
        externalUserId: String,
        email: String,
        name: String,
        customProperties: Map<String, Any?>? = null
    ): Customer? {
        if (!isInitialized) {
            logError("Failed to identify user: SDK not initialized. Call initialize() first.")
            return null
        }

        if (externalUserId.isBlank()) {
            logError("Failed to identify user: externalUserId cannot be empty")
            return null
        }

        if (email.isBlank()) {
            logError("Failed to identify user: email cannot be empty")
            return null
        }

        val request = IdentifyRequest(
            externalUserId = externalUserId,
            email = email,
            name = name,
            customProperties = customProperties
        )

        return try {
            withContext(Dispatchers.IO) {
                val customerService = CustomerService(publicAccessKey, apiBaseUrl)
                val campaignService = CampaignService(publicAccessKey, apiBaseUrl)

                val identifyResult = customerService.identify(request)
                customer = identifyResult

                val campaignResult =
                    campaignService.getLiveCampaign(environment, identifyResult.id)
                campaign = campaignResult

                isIdentified = true

                processIdentifyCompletionQueue()
                logInfo("Customer identified: $identifyResult")
                identifyResult
            }
        } catch (e: Exception) {
            logError("Failed to identify user or fetch campaign", e)
            null
        }
    }

    /**
     * Opens a Flock webpage in a full-screen modal.
     *
     * @deprecated Use addPlacement instead.
     */
    @Deprecated("Use addPlacement instead.")
    fun openPage(context: Context, pageType: String, callback: FlockWebViewCallback? = null) {
        if (!isInitialized) {
            logError("Cannot open page: SDK not initialized. Call initialize() first.")
            return
        }

        if (pageType.isBlank()) {
            logError("Cannot open page: pageType cannot be empty")
            return
        }

        if (!isIdentified) {
            logWarn("User not identified yet. Queueing openPage call.")
            identifyCompletionQueue.add {
                openPage(context, pageType, callback)
            }
            return
        }

        try {
            val path = pageType.split("?").first()
            val query = pageType.split("?").getOrElse(1) { "" }
            val campaignPage = campaign?.campaignPages?.find { it.path.contains(pageType) }
            val backgroundColor = campaignPage?.screenProps?.backgroundColor
            val url = buildString {
                append("$uiBaseUrl/pages/$path?key=$publicAccessKey&campaign_id=${campaign?.id}&customer_id=${customer?.id}")
                if (backgroundColor != null) {
                    append("&bg=${android.net.Uri.encode(backgroundColor)}")
                }
                if (query.isNotBlank()) {
                    append("&$query")
                }
            }

            FlockWebViewActivity.callback = callback
            FlockWebViewActivity.backgroundColorHex = backgroundColor
            FlockWebViewActivity.start(context, url)
            logDebug("Opening page $pageType with URL: $url")
        } catch (e: Exception) {
            logError("Error opening page $pageType", e)
        }
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
    fun addPlacement(
        context: Context,
        placementId: String,
        callback: FlockWebViewCallback? = null
    ) {
        if (!isInitialized) {
            val error = "SDK not initialized. Call initialize() first."
            logError(error)
            return
        }

        if (placementId.isBlank()) {
            val error = "Placement ID cannot be empty"
            logError(error)
            return
        }

        if (!isIdentified) {
            logWarn("User not identified yet. Queueing addPlacement call.")
            identifyCompletionQueue.add {
                addPlacement(context, placementId, callback)
            }
            return
        }

        try {
            val campaignPlacement = campaign?.campaignPages?.find { it.placementId == placementId }
            if (campaignPlacement == null) {
                logWarn("Placement $placementId not found in campaign")
                return
            }

            val url = buildPlacementUrl(campaignPlacement)

            FlockWebViewActivity.callback = callback
            FlockWebViewActivity.backgroundColorHex = campaignPlacement.screenProps?.backgroundColor
            FlockWebViewActivity.start(context, url)
            logDebug("Adding placement $placementId with URL: $url")
        } catch (e: Exception) {
            logError("Error opening placement $placementId", e)
        }
    }

    /**
     * Navigates to a placement within the same web view.
     *
     * @param placementId The placement ID to navigate to
     *
     * Example usage:
     * ```kotlin
     * FlockSDK.navigate("your_placement_id")
     * ```
     */
    fun navigate(placementId: String) {
        if (!isInitialized) {
            val error = "SDK not initialized. Call initialize() first."
            logError(error)
            return
        }

        if (!isIdentified) {
            logError("Cannot navigate: User not identified")
            return
        }

        if (placementId.isBlank()) {
            logError("Placement ID cannot be empty")
            return
        }

        try {
            // Try to get background color for placement if available (fallback to null)
            val campaignPlacement = campaign?.campaignPages?.find { it.placementId == placementId }
            if (campaignPlacement == null) {
                logError("Placement $placementId not found in campaign")
                return
            }

            val url = buildPlacementUrl(campaignPlacement)

            FlockEventBus.postNavigate(url, backgroundColorHex = campaignPlacement.screenProps?.backgroundColor)

            logDebug("Navigating to placement $placementId with URL: $url")
        } catch (e: Exception) {
            logError("Error navigating to placement $placementId", e)
        }
    }

    /**
     * Builds a placement URL with the necessary parameters
     *
     * @param placement The placement to build the URL for
     * @return The fully constructed URL string
     */
    private fun buildPlacementUrl(placement: CampaignPage): String {
        val backgroundColor = placement.screenProps?.backgroundColor

        return buildString {
            append("$uiBaseUrl/placements/${placement.placementId}?key=$publicAccessKey&campaign_id=${campaign?.id}&customer_id=${customer?.id}")
            if (backgroundColor != null) {
                append("&bg=${Uri.encode(backgroundColor)}")
            }
        }
    }

    private fun processIdentifyCompletionQueue() {
        val queueCopy = identifyCompletionQueue.toList()
        identifyCompletionQueue.clear()
        queueCopy.forEach { it() }
        logDebug("Processed identify completion queue")
    }

    /**
     * Enable or disable logging for the SDK
     *
     * @param enabled Whether logging should be enabled
     * @param level The minimum log level to display (default: INFO)
     */
    fun setLoggingEnabled(enabled: Boolean, level: LogLevel = LogLevel.INFO) {
        loggingEnabled = enabled
        logLevel = level
        logInfo("Logging ${if (enabled) "enabled" else "disabled"} at level $level")
    }

    // Internal logging methods
    private fun logVerbose(message: String) {
        if (loggingEnabled && logLevel.ordinal <= LogLevel.VERBOSE.ordinal) {
            Log.v(TAG, message)
        }
    }

    private fun logDebug(message: String) {
        if (loggingEnabled && logLevel.ordinal <= LogLevel.DEBUG.ordinal) {
            Log.d(TAG, message)
        }
    }

    private fun logInfo(message: String) {
        if (loggingEnabled && logLevel.ordinal <= LogLevel.INFO.ordinal) {
            Log.i(TAG, message)
        }
    }

    private fun logWarn(message: String) {
        if (loggingEnabled && logLevel.ordinal <= LogLevel.WARN.ordinal) {
            Log.w(TAG, message)
        }
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        if (loggingEnabled && logLevel.ordinal <= LogLevel.ERROR.ordinal) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }
}