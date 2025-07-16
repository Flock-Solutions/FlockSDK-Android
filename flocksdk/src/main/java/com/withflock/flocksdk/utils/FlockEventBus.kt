package com.withflock.flocksdk.utils

typealias NavigateListener = (placementId: String, backgroundColorHex: String?) -> Unit

internal object FlockEventBus {
    private val listeners = mutableSetOf<NavigateListener>()

    fun postNavigate(placementId: String, backgroundColorHex: String?) {
        listeners.forEach { it(placementId, backgroundColorHex) }
    }

    fun registerNavigateListener(listener: NavigateListener) {
        listeners.add(listener)
    }

    fun unregisterNavigateListener(listener: NavigateListener) {
        listeners.remove(listener)
    }
}
