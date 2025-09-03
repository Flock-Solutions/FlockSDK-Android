package com.withflock.flocksdk.utils

import android.content.Context
import com.withflock.flocksdk.FlockSDK

/**
 * Builder class for creating and triggering checkpoints.
 * Provides a fluent interface for configuring checkpoint callbacks.
 */
class CheckpointBuilder(
    private val checkpointName: String,
    private val flockSDK: FlockSDK
) {
    private var options = CheckpointOptions()
    private var onCloseCallback: (() -> Unit)? = null
    private var onSuccessCallback: (() -> Unit)? = null
    private var onInvalidCallback: (() -> Unit)? = null

    /**
     * Sets a callback to be invoked when the checkpoint UI is closed.
     *
     * @param callback The function to call when the checkpoint UI is closed
     * @return This CheckpointBuilder instance for method chaining
     */
    fun onClose(callback: () -> Unit): CheckpointBuilder {
        onCloseCallback = callback
        return this
    }

    /**
     * Sets a callback to be invoked when the checkpoint is successfully completed.
     *
     * @param callback The function to call when the checkpoint is successful
     * @return This CheckpointBuilder instance for method chaining
     */
    fun onSuccess(callback: () -> Unit): CheckpointBuilder {
        onSuccessCallback = callback
        return this
    }

    /**
     * Sets a callback to be invoked when the checkpoint is invalid.
     *
     * @param callback The function to call when the checkpoint is invalid
     * @return This CheckpointBuilder instance for method chaining
     */
    fun onInvalid(callback: () -> Unit): CheckpointBuilder {
        onInvalidCallback = callback
        return this
    }

    /**
     * Sets this checkpoint to navigate instead of opening a new UI.
     *
     * @param shouldNavigate Whether to navigate instead of opening a new UI (defaults to true)
     * @return This CheckpointBuilder instance for method chaining
     */
    fun navigate(shouldNavigate: Boolean = true): CheckpointBuilder {
        options = CheckpointOptions(navigate = shouldNavigate)
        return this
    }

    /**
     * Triggers the checkpoint, opening the associated placement if one exists.
     * 
     * @param context The Android context needed to open UI components (required for placement triggers)
     */
    fun trigger(context: Context) {
        flockSDK.triggerCheckpoint(
            context = context,
            checkpointName = checkpointName,
            options = options,
            onClose = onCloseCallback,
            onSuccess = onSuccessCallback,
            onInvalid = onInvalidCallback
        )
    }
}
