package com.withflock.flocksdk.model

data class IdentifyRequest(
    val externalUserId: String,
    val email: String,
    val name: String
)
