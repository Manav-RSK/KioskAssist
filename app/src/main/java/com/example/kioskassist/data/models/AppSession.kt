package com.example.kioskassist.data.models

data class AppSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long
) {
    val duration: Long
        get() = endTime - startTime
}