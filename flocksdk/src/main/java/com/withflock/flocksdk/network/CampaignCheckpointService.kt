package com.withflock.flocksdk.network

import com.google.gson.Gson
import com.withflock.flocksdk.model.CampaignCheckpoint
import com.withflock.flocksdk.model.CampaignCheckpointsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Service for interacting with campaign checkpoint related endpoints.
 */
internal class CampaignCheckpointService(
    private val publicAccessKey: String, 
    private val baseUrl: String = "https://api.withflock.com"
) {
    private val client = OkHttpClient()
    private val gson = Gson()

    /**
     * Fetches all checkpoints for a specific campaign.
     *
     * @param campaignId The ID of the campaign to fetch checkpoints for
     * @return List of campaign checkpoints
     * @throws Exception if the request fails or response parsing fails
     */
    suspend fun getCampaignCheckpoints(campaignId: String): List<CampaignCheckpoint> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/campaign-checkpoints".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("campaignId", campaignId)
            .build()
            .toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", publicAccessKey)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to fetch campaign checkpoints: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty response body")

        val checkpointsResponse = gson.fromJson(responseBody, CampaignCheckpointsResponse::class.java)
        return@withContext checkpointsResponse.data
    }
}
