package com.withflock.flocksdk.utils

/**
 * Options for configuring checkpoint behavior.
 */
data class CheckpointOptions(
    /**
     * Whether to navigate within an existing WebView instead of opening a new one.
     */
    val navigate: Boolean = false
)
