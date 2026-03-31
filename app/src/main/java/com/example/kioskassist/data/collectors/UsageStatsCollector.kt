package com.example.kioskassist.data.collectors

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.kioskassist.data.models.AppSession
import com.example.kioskassist.data.models.AppUsage
import com.example.kioskassist.data.models.DailyUsage
import java.util.Calendar

class UsageStatsCollector(private val context: Context) {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    /**
     * Check if the app has usage stats permission
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Get total screen time for today in formatted string
     */
    fun getTodayScreenTimeFormatted(): String {
        val dailyUsage = getTodayUsage()
        val totalTime = dailyUsage.totalScreenTimeMillis

        val hours = totalTime / (1000 * 60 * 60)
        val minutes = (totalTime / (1000 * 60)) % 60

        return "${hours}h ${minutes}m"
    }

    /**
     * Get today's usage data
     */
    fun getTodayUsage(): DailyUsage {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return getUsageForPeriod(startTime, endTime)
    }

    /**
     * Get usage data for a specific period
     */
    fun getUsageForPeriod(startTime: Long, endTime: Long): DailyUsage {
        if (!hasUsageStatsPermission()) {
            // Return empty data if permission not granted
            return DailyUsage(
                date = startTime,
                totalScreenTimeMillis = 0L,
                appUsages = emptyList()
            )
        }

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTime = 0L
        val appUsages = mutableListOf<AppUsage>()

        usageStatsList?.forEach { usageStats ->
            val timeInForeground = usageStats.totalTimeInForeground
            if (timeInForeground > 0) {
                totalTime += timeInForeground
                appUsages.add(
                    AppUsage(
                        packageName = usageStats.packageName,
                        appName = getAppName(usageStats.packageName),
                        totalTimeMillis = timeInForeground,
                        launchCount = usageStats.lastTimeUsed.toInt() // Hardcoded for now
                    )
                )
            }
        }

        // Sort by total time descending
        appUsages.sortByDescending { it.totalTimeMillis }

        return DailyUsage(
            date = startTime,
            totalScreenTimeMillis = totalTime,
            appUsages = appUsages
        )
    }

    /**
     * Get app sessions (hardcoded for now as detailed session data requires more complex tracking)
     */
    fun getAppSessions(packageName: String, startTime: Long, endTime: Long): List<AppSession> {
        // This would require complex event tracking in production
        // For now, return hardcoded sessions based on total usage
        val dailyUsage = getUsageForPeriod(startTime, endTime)
        val appUsage = dailyUsage.appUsages.find { it.packageName == packageName }

        if (appUsage != null && appUsage.totalTimeMillis > 0) {
            // Hardcode a single session representing the total time
            return listOf(
                AppSession(
                    packageName = packageName,
                    startTime = startTime,
                    endTime = startTime + appUsage.totalTimeMillis
                )
            )
        }

        return emptyList()
    }

    /**
     * Get app name from package name
     */
    private fun getAppName(packageName: String): String? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get total screen time in milliseconds for today
     */
    fun getTodayScreenTimeMillis(): Long {
        return getTodayUsage().totalScreenTimeMillis
    }
}

