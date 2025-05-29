package com.withflock.flocksdk.model

import com.withflock.flocksdk.ScreenProps

data class CampaignPage(
    val path: String,
    val id: String,
    val isEmpty: Boolean,
    val screenProps: ScreenProps? = null
)
