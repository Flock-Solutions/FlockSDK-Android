package com.withflock.flocksdk

import com.google.gson.annotations.SerializedName

enum class FlockEnvironment {
    @SerializedName("test")
    TEST,

    @SerializedName("production")
    PRODUCTION;

    override fun toString(): String = when (this) {
        TEST -> "test"
        PRODUCTION -> "production"
    }
}