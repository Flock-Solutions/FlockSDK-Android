package com.withflock.flocksdk.network

import android.util.Log
import com.google.gson.Gson
import com.withflock.flocksdk.FlockEnvironment
import com.withflock.flocksdk.model.Campaign
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

internal class CampaignService(private val publicAccessKey: String, private val baseUrl: String = "https://api.withflock.com") {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getLiveCampaign(environment: FlockEnvironment, customerId: String): Campaign {
        val url = "$baseUrl/campaigns/live".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("environment", environment.name.lowercase())
            .addQueryParameter("customerId", customerId)
            .build()
            .toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", publicAccessKey)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Failed to fetch campaign: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty response body")

        return gson.fromJson(responseBody, Campaign::class.java)
    }

    fun ping(campaignId: String) {
        val url = "$baseUrl/campaigns/$campaignId/ping".toHttpUrlOrNull()!!.toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", publicAccessKey)
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw RuntimeException("Ping failed: ${response.code}")
        }
    }
}
