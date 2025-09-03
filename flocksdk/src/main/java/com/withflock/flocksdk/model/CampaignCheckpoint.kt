package com.withflock.flocksdk.model

import com.google.gson.annotations.SerializedName

/**
 * Enum representing the trigger type for a checkpoint.
 */
enum class CheckpointTrigger {
    @SerializedName("placement")
    PLACEMENT,
    
    @SerializedName("reward")
    REWARD
}

/**
 * Data class representing a campaign checkpoint.
 */
data class CampaignCheckpoint(
    val id: String,
    val campaignId: String,
    val checkpointName: String,
    val trigger: CheckpointTrigger,
    val placementId: String?,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Response wrapper for campaign checkpoints.
 */
data class CampaignCheckpointsResponse(
    val data: List<CampaignCheckpoint>
)
