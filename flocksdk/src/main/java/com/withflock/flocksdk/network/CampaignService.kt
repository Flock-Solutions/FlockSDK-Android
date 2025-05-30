package com.withflock.flocksdk.network

import com.google.gson.Gson
import com.withflock.flocksdk.FlockEnvironment
import com.withflock.flocksdk.model.Campaign
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

internal class CampaignService(private val publicAccessKey: String, private val baseUrl: String = "https://api.withflock.com") {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getLiveCampaign(environment: FlockEnvironment): Campaign {
        val url = "$baseUrl/campaigns/live".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("environment", environment.name.lowercase())
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
}
