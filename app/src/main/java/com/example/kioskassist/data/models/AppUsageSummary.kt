package com.example.kioskassist.data.models

data class AppUsageSummary(
    val appName: String,
    val usageMinutes: Long,
    val launches: Int,
    val usagePercentage: Float
)