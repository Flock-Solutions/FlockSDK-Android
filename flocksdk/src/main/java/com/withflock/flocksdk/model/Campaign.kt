package com.withflock.flocksdk.model

import com.withflock.flocksdk.FlockEnvironment

data class Campaign(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val environment: FlockEnvironment,
    val isLive: Boolean,
    val campaignPages: List<CampaignPage>
)
