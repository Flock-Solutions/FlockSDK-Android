package com.withflock.flocksdk.utils

typealias NavigateListener = (pageType: String) -> Unit

object FlockEventBus {
    private val listeners = mutableSetOf<NavigateListener>()

    fun postNavigate(pageType: String) {
        listeners.forEach { it(pageType) }
    }

    fun registerNavigateListener(listener: NavigateListener) {
        listeners.add(listener)
    }

    fun unregisterNavigateListener(listener: NavigateListener) {
        listeners.remove(listener)
    }
}
