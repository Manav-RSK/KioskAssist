package com.example.kioskassist.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Manages all permission-related operations
 */
class PermissionManager(private val activity: ComponentActivity) {

    private val context: Context = activity
    val permissionChecker = PermissionChecker(context)
    
    private var permissionCallback: ((String, Boolean) -> Unit)? = null

    // Permission launchers
    private val storagePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionCallback?.invoke("Storage", allGranted)
    }

    private val telephonePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Telephone", isGranted)
    }

    private val contactsPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Contacts", isGranted)
    }

    private val microphonePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Microphone", isGranted)
    }

    private val callLogPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Call Log", isGranted)
    }

    private val smsPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionCallback?.invoke("SMS", allGranted)
    }

    private val cameraPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Camera", isGranted)
    }

    private val locationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        permissionCallback?.invoke("Location", allGranted)
    }

    private val backgroundLocationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Background Location", isGranted)
    }

    private val notificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionCallback?.invoke("Enable Notification", isGranted)
    }

    /**
     * Request permission by name
     */
    fun requestPermission(permissionName: String, callback: (String, Boolean) -> Unit) {
        permissionCallback = callback
        
        when (permissionName) {
            "Storage" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermissionLauncher.launch(arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ))
                } else {
                    storagePermissionLauncher.launch(arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }
            "Telephone" -> telephonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            "Contacts" -> contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            "Microphone" -> microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            "Call Log" -> callLogPermissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
            "SMS" -> smsPermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ))
            "Camera" -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            "Location" -> locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            "Background Location" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    permissionCallback?.invoke(permissionName, true)
                }
            }
            "Ignore Battery Optimization" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    activity.startActivity(intent)
                } else {
                    permissionCallback?.invoke(permissionName, true)
                }
            }
            "Enable Notification" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Open notification settings for older versions
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    activity.startActivity(intent)
                }
            }
            "Display Over Other Apps" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    activity.startActivity(intent)
                }
            }
            "Installed Application Information" -> {
                // This is typically always granted
                permissionCallback?.invoke(permissionName, true)
            }
            "Usage Access" -> {
                activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
    }
}

