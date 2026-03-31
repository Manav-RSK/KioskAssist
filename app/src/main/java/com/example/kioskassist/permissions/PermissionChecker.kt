package com.example.kioskassist.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.kioskassist.data.repository.UsageRepository

/**
 * Helper class to check permission status
 */
class PermissionChecker(private val context: Context) {

    private val usageRepository by lazy { UsageRepository(context) }

    /**
     * Check if a permission is granted
     */
    fun checkPermissionStatus(permission: Permission): Boolean {
        return when (permission.name) {
            "Storage" -> {
                permission.androidPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }
            }
            "Telephone", "Contacts", "Microphone", "Call Log", "Camera" -> {
                permission.androidPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }
            }
            "SMS" -> {
                permission.androidPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }
            }
            "Location" -> {
                permission.androidPermissions.all { perm ->
                    ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                }
            }
            "Background Location" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Not needed on older versions
                }
            }
            "Ignore Battery Optimization" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
                } else {
                    true
                }
            }
            "Enable Notification" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    // Check if notifications are enabled using NotificationManagerCompat
                    androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
            "Display Over Other Apps" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    true
                }
            }
            "Installed Application Information" -> {
                // This is generally always available
                true
            }
            "Usage Access" -> {
                usageRepository.hasUsageStatsPermission()
            }
            else -> false
        }
    }
}

