package com.example.kioskassist.data.models

data class AppUsage(
    val packageName: String,
    val appName: String? = null,   // resolve later using PackageManager
    val totalTimeMillis: Long,
    val launchCount: Int = 0
)