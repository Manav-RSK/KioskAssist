package com.example.kioskassist.examples

import android.content.Context
import com.example.kioskassist.data.repository.UsageRepository

/**
 * Example usage of the new UsageRepository architecture
 *
 * This file demonstrates how to use the repository pattern
 * to fetch usage statistics data according to our models.
 */

class UsageExamples {

    /**
     * Example 1: Get today's screen time (formatted string)
     */
    fun exampleGetTodayScreenTime(context: Context) {
        val repository = UsageRepository(context)

        if (repository.hasUsageStatsPermission()) {
            val screenTime = repository.getTodayScreenTime()
            // Returns: "5h 23m"
            println("Screen Time Today: $screenTime")
        } else {
            println("Permission not granted")
        }
    }

    /**
     * Example 2: Get complete daily usage data
     */
    fun exampleGetDailyUsageData(context: Context) {
        val repository = UsageRepository(context)

        if (repository.hasUsageStatsPermission()) {
            val dailyUsage = repository.getTodayUsageData()

            // Total screen time in milliseconds
            println("Total Screen Time: ${dailyUsage.totalScreenTimeMillis}ms")

            // Date timestamp
            println("Date: ${dailyUsage.date}")

            // All app usages (sorted by time, descending)
            dailyUsage.appUsages.forEach { appUsage ->
                println("App: ${appUsage.appName ?: appUsage.packageName}")
                println("  Time: ${appUsage.totalTimeMillis}ms")
                println("  Launches: ${appUsage.launchCount}")
            }
        }
    }

    /**
     * Example 3: Get usage for a specific time period
     */
    fun exampleGetUsageForPeriod(context: Context) {
        val repository = UsageRepository(context)

        // Get usage for last 7 days
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L) // 7 days ago

        if (repository.hasUsageStatsPermission()) {
            val usage = repository.getUsageForPeriod(startTime, endTime)

            println("Usage from $startTime to $endTime:")
            println("Total: ${usage.totalScreenTimeMillis}ms")
            println("Apps: ${usage.appUsages.size}")
        }
    }

    /**
     * Example 4: Get app sessions for specific app (hardcoded for now)
     */
    fun exampleGetAppSessions(context: Context) {
        val repository = UsageRepository(context)

        val packageName = "com.android.chrome"
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000L) // Last 24 hours

        if (repository.hasUsageStatsPermission()) {
            val sessions = repository.getAppSessions(packageName, startTime, endTime)

            sessions.forEach { session ->
                println("Session for $packageName:")
                println("  Start: ${session.startTime}")
                println("  End: ${session.endTime}")
                println("  Duration: ${session.duration}ms")
            }
        }
    }

    /**
     * Example 5: Get top 5 most used apps
     */
    fun exampleGetTopApps(context: Context) {
        val repository = UsageRepository(context)

        if (repository.hasUsageStatsPermission()) {
            val dailyUsage = repository.getTodayUsageData()

            // Already sorted by usage time (descending)
            val topApps = dailyUsage.appUsages.take(5)

            println("Top 5 Most Used Apps:")
            topApps.forEachIndexed { index, app ->
                val minutes = app.totalTimeMillis / (1000 * 60)
                println("${index + 1}. ${app.appName ?: app.packageName} - ${minutes} minutes")
            }
        }
    }

    /**
     * Example 6: Calculate usage percentage for each app
     */
    fun exampleCalculateUsagePercentages(context: Context) {
        val repository = UsageRepository(context)

        if (repository.hasUsageStatsPermission()) {
            val dailyUsage = repository.getTodayUsageData()
            val totalTime = dailyUsage.totalScreenTimeMillis

            if (totalTime > 0) {
                dailyUsage.appUsages.forEach { app ->
                    val percentage = (app.totalTimeMillis.toFloat() / totalTime) * 100
                    println("${app.appName ?: app.packageName}: ${"%.2f".format(percentage)}%")
                }
            }
        }
    }

    /**
     * Example 7: Get screen time in different formats
     */
    fun exampleDifferentFormats(context: Context) {
        val repository = UsageRepository(context)

        if (repository.hasUsageStatsPermission()) {
            // Format 1: Formatted string
            val formatted = repository.getTodayScreenTime()
            println("Formatted: $formatted")  // "5h 23m"

            // Format 2: Milliseconds
            val millis = repository.getTodayScreenTimeMillis()
            println("Milliseconds: $millis")

            // Format 3: Convert to different units
            val hours = millis / (1000 * 60 * 60)
            val minutes = (millis / (1000 * 60)) % 60
            val seconds = (millis / 1000) % 60
            println("Detailed: ${hours}h ${minutes}m ${seconds}s")
        }
    }

    /**
     * Example 8: Future database usage (when implemented)
     */
    fun exampleFutureDatabaseUsage(context: Context) {
        val repository = UsageRepository(context)

        // When database is implemented, just change useDatabase flag to true
        // The same API calls will work, but data will come from DB

        // For now, this still works using UsageStatsCollector
        val dailyUsage = repository.getTodayUsageData()

        // In future, you could save to DB like this:
        // repository.saveUsageData(dailyUsage)

        println("Using repository pattern makes it easy to switch data sources!")
    }
}

