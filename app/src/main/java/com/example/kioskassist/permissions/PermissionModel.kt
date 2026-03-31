package com.example.kioskassist.permissions

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a permission
 */
data class Permission(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val isGranted: Boolean = false,
    val isSpecialPermission: Boolean = false,
    val isDisabled: Boolean = false,
    val androidPermissions: List<String> = emptyList()
)

/**
 * Get all permissions required by the app
 */
fun getAllPermissions(): List<Permission> {
    return listOf(
        Permission(
            "Storage",
            Icons.Default.Star,
            "Access device storage",
            androidPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        ),
        Permission(
            "Telephone",
            Icons.Default.Phone,
            "Make and manage phone calls",
            androidPermissions = listOf(Manifest.permission.READ_PHONE_STATE)
        ),
        Permission(
            "Contacts",
            Icons.Default.Person,
            "Access your contacts",
            androidPermissions = listOf(Manifest.permission.READ_CONTACTS)
        ),
        Permission(
            "Microphone",
            Icons.Default.Settings,
            "Record audio",
            androidPermissions = listOf(Manifest.permission.RECORD_AUDIO)
        ),
        Permission(
            "Call Log",
            Icons.Default.Call,
            "Access call logs",
            androidPermissions = listOf(Manifest.permission.READ_CALL_LOG)
        ),
        Permission(
            "SMS",
            Icons.Default.Email,
            "Send and view SMS messages",
            androidPermissions = listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS)
        ),
        Permission(
            "Camera",
            Icons.Default.Settings,
            "Take pictures and record videos",
            androidPermissions = listOf(Manifest.permission.CAMERA)
        ),
        Permission(
            "Location",
            Icons.Default.LocationOn,
            "Access device location",
            androidPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ),
        Permission(
            "Ignore Battery Optimization",
            Icons.Default.Build,
            "Run in background without restrictions",
            isSpecialPermission = true
        ),
        Permission(
            "Enable Notification",
            Icons.Default.Notifications,
            "Show notifications",
            isSpecialPermission = true
        ),
        Permission(
            "Display Over Other Apps",
            Icons.Default.Settings,
            "Display content over other apps",
            isSpecialPermission = true
        ),
        Permission(
            "Background Location",
            Icons.Default.Place,
            "Access location in background",
            androidPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else emptyList()
        ),
        Permission(
            "Installed Application Information",
            Icons.Default.Info,
            "Access installed apps information",
            isSpecialPermission = true
        ),
        Permission(
            "Usage Access",
            Icons.Default.Settings,
            "Access app usage statistics and screen time",
            isSpecialPermission = true
        )
    )
}

