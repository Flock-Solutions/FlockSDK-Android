package com.withflock.flocksdk.model

data class CampaignPage(
    val path: String,
    val id: String,
    val isEmpty: Boolean,
    val screenProps: ScreenProps? = null
)
