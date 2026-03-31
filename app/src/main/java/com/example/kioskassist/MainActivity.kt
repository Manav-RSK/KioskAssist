package com.example.kioskassist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskassist.data.repository.UsageRepository
import com.example.kioskassist.permissions.Permission
import com.example.kioskassist.permissions.PermissionManager
import com.example.kioskassist.permissions.getAllPermissions
import com.example.kioskassist.ui.theme.KioskAssistTheme

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize permission manager
        permissionManager = PermissionManager(this)

        setContent {
            KioskAssistTheme {
                var currentScreen by remember { mutableStateOf("permissions") }

                when (currentScreen) {
                    "permissions" -> PermissionsScreen(
                        activity = this,
                        permissionManager = permissionManager,
                        onContinue = { currentScreen = "main" }
                    )
                    "main" -> MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    activity: ComponentActivity,
    permissionManager: PermissionManager,
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current
    val usageRepository = remember { UsageRepository(context) }

    // Track when we return from settings to check permission status
    var checkPermissions by remember { mutableStateOf(false) }

    // Track if this is the initial load
    var isInitialLoad by remember { mutableStateOf(true) }

    // Lifecycle observer to check permissions when returning from settings
    DisposableEffect(Unit) {
        val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                checkPermissions = true
            }
        }
        activity.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            activity.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    val permissions = remember {
        mutableStateListOf(*getAllPermissions().toTypedArray())
    }

    // Check all permissions status when returning from settings
    LaunchedEffect(checkPermissions) {
        if (checkPermissions && !isInitialLoad) {
            // Update permission statuses when returning from settings
            // But do NOT auto-navigate even if all are granted
            permissions.forEachIndexed { index, permission ->
                val isGranted = permissionManager.permissionChecker.checkPermissionStatus(permission)
                permissions[index] = permissions[index].copy(
                    isGranted = isGranted,
                    isDisabled = isGranted
                )
            }
            checkPermissions = false
        }
    }

    // Initial permission check on first load
    LaunchedEffect(Unit) {
        permissions.forEachIndexed { index, permission ->
            val isGranted = permissionManager.permissionChecker.checkPermissionStatus(permission)
            permissions[index] = permissions[index].copy(
                isGranted = isGranted,
                isDisabled = isGranted
            )
        }

        // Check if all permissions are already granted on initial load
        val allGranted = permissions.all { it.isGranted }
        if (allGranted) {
            // All permissions already granted, navigate directly to home screen
            onContinue()
        } else {
            // Not all granted, mark initial load complete
            isInitialLoad = false
        }
    }

    val allPermissionsGranted = permissions.all { it.isGranted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Permissions Required",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Text
            Text(
                text = "Please grant the following permissions to use this app",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Scrollable Permission List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(permissions.size) { index ->
                    PermissionItem(
                        permission = permissions[index],
                        onToggle = {
                            val permission = permissions[index]
                            // Request the actual Android permission using PermissionManager
                            permissionManager.requestPermission(permission.name) { permissionName, isGranted ->
                                val idx = permissions.indexOfFirst { it.name == permissionName }
                                if (idx != -1) {
                                    permissions[idx] = permissions[idx].copy(
                                        isGranted = isGranted,
                                        isDisabled = isGranted
                                    )
                                }
                            }
                        }
                    )
                }

                // Add spacing at the bottom of the list
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Continue Button
            Button(
                onClick = {
                    if (allPermissionsGranted) {
                        // All permissions granted, log data and continue
                        logCompleteUsageData(usageRepository)
                        onContinue()
                    }
                },
                enabled = allPermissionsGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (allPermissionsGranted) "Continue" else "Grant All Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PermissionItem(
    permission: Permission,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = permission.icon,
                contentDescription = permission.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = permission.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = permission.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Checkbox(
                checked = permission.isGranted,
                onCheckedChange = { if (!permission.isDisabled) onToggle() },
                enabled = !permission.isDisabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kiosk Assist",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Kiosk Assist!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "All permissions granted successfully",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Log complete usage data with all details
 */
fun logCompleteUsageData(repository: UsageRepository) {
    val TAG = "ScreenTimeData"

    Log.d(TAG, "========================================")
    Log.d(TAG, "SCREEN TIME USAGE DATA")
    Log.d(TAG, "========================================")

    try {
        // Get today's complete usage data
        val dailyUsage = repository.getTodayUsageData()

        // Log total screen time
        val totalMillis = dailyUsage.totalScreenTimeMillis
        val totalHours = totalMillis / (1000 * 60 * 60)
        val totalMinutes = (totalMillis / (1000 * 60)) % 60
        val totalSeconds = (totalMillis / 1000) % 60

        Log.d(TAG, "📱 TOTAL SCREEN TIME")
        Log.d(TAG, "  ├─ Formatted: ${repository.getTodayScreenTime()}")
        Log.d(TAG, "  ├─ Milliseconds: $totalMillis ms")
        Log.d(TAG, "  ├─ Detailed: ${totalHours}h ${totalMinutes}m ${totalSeconds}s")
        Log.d(TAG, "  └─ Date: ${dailyUsage.date} (timestamp)")
        Log.d(TAG, "")

        // Log app count
        val appCount = dailyUsage.appUsages.size
        Log.d(TAG, "📊 TOTAL APPS USED: $appCount")
        Log.d(TAG, "")

        if (appCount == 0) {
            Log.d(TAG, "⚠️ No app usage data available")
            Log.d(TAG, "========================================")
            return
        }

        // Log per-app usage details
        Log.d(TAG, "📱 PER-APP USAGE DETAILS")
        Log.d(TAG, "----------------------------------------")

        dailyUsage.appUsages.forEachIndexed { index, appUsage ->
            val appMillis = appUsage.totalTimeMillis
            val appHours = appMillis / (1000 * 60 * 60)
            val appMinutes = (appMillis / (1000 * 60)) % 60
            val appSeconds = (appMillis / 1000) % 60
            val percentage = if (totalMillis > 0) {
                (appUsage.totalTimeMillis.toFloat() / totalMillis * 100)
            } else 0f

            Log.d(TAG, "")
            Log.d(TAG, "${index + 1}. ${appUsage.appName ?: "Unknown App"}")
            Log.d(TAG, "  ├─ Package: ${appUsage.packageName}")
            Log.d(TAG, "  ├─ Time: ${appHours}h ${appMinutes}m ${appSeconds}s")
            Log.d(TAG, "  ├─ Milliseconds: ${appMillis} ms")
            Log.d(TAG, "  ├─ Percentage: ${"%.2f".format(percentage)}%")
            Log.d(TAG, "  └─ Launch Count: ${appUsage.launchCount}")
        }

        Log.d(TAG, "")
        Log.d(TAG, "========================================")

        // Log top 5 apps summary
        val top5 = dailyUsage.appUsages.take(5)
        if (top5.isNotEmpty()) {
            Log.d(TAG, "")
            Log.d(TAG, "🏆 TOP 5 MOST USED APPS")
            Log.d(TAG, "----------------------------------------")
            top5.forEachIndexed { index, app ->
                val minutes = app.totalTimeMillis / (1000 * 60)
                val percentage = if (totalMillis > 0) {
                    (app.totalTimeMillis.toFloat() / totalMillis * 100)
                } else 0f
                Log.d(TAG, "${index + 1}. ${app.appName ?: app.packageName}")
                Log.d(TAG, "     ${minutes} min (${"%.1f".format(percentage)}%)")
            }
            Log.d(TAG, "========================================")
        }

        // Log statistics
        Log.d(TAG, "")
        Log.d(TAG, "📈 STATISTICS")
        Log.d(TAG, "----------------------------------------")
        if (appCount > 0) {
            val avgUsageMillis = totalMillis / appCount
            val avgMinutes = avgUsageMillis / (1000 * 60)
            val longestApp = dailyUsage.appUsages.maxByOrNull { it.totalTimeMillis }
            val shortestApp = dailyUsage.appUsages.minByOrNull { it.totalTimeMillis }

            Log.d(TAG, "  ├─ Average usage per app: $avgMinutes minutes")
            Log.d(TAG, "  ├─ Longest used: ${longestApp?.appName ?: "N/A"} (${longestApp?.totalTimeMillis?.div(1000 * 60)} min)")
            Log.d(TAG, "  └─ Shortest used: ${shortestApp?.appName ?: "N/A"} (${shortestApp?.totalTimeMillis?.div(1000 * 60)} min)")
        }
        Log.d(TAG, "========================================")

    } catch (e: Exception) {
        Log.e(TAG, "Error logging usage data: ${e.message}", e)
        Log.e(TAG, "========================================")
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    KioskAssistTheme {
        // Preview without activity parameter - won't have lifecycle observer
        MainScreen()
    }
}