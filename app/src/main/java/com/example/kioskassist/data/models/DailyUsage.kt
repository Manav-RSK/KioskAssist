package com.example.kioskassist.data.models

data class DailyUsage(
    val date: Long, // start of day timestamp
    val totalScreenTimeMillis: Long,
    val appUsages: List<AppUsage>
)