package com.withflock.flocksdk.model

import com.withflock.flocksdk.FlockEnvironment

data class IdentifyRequest(
    val externalUserId: String,
    val email: String,
    val name: String,
    val environment: FlockEnvironment,
    /**
     * Custom properties for this identify request. Allowed value types: String, Number, Boolean, or null.
     */
    val customProperties: Map<String, Any?>? = null
)
