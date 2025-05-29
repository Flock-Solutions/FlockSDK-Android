package com.withflock.flocksdk.network

import com.google.gson.Gson
import com.withflock.flocksdk.model.IdentifyRequest
import com.withflock.flocksdk.model.Customer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CustomerService(private val publicAccessKey: String, private val baseUrl: String = "https://api.withflock.com") {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun identify(request: IdentifyRequest): Customer {
        val url = "$baseUrl/customers/identify"
        val json = gson.toJson(request)
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", publicAccessKey)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(httpRequest).execute()

        if (!response.isSuccessful) {
            throw RuntimeException("Failed to identify customer: ${response.code}")
        }

        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty response body")

        return gson.fromJson(responseBody, Customer::class.java)
    }
}