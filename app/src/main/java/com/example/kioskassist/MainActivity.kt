package com.example.kioskassist

import android.os.Bundle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kioskassist.ui.theme.KioskAssistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KioskAssistTheme {
                var currentScreen by remember { mutableStateOf("permissions") }

                when (currentScreen) {
                    "permissions" -> PermissionsScreen(
                        onContinue = { currentScreen = "main" }
                    )
                    "main" -> MainScreen()
                }
            }
        }
    }
}

data class Permission(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val isGranted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(onContinue: () -> Unit = {}) {
    val permissions = remember {
        mutableStateListOf(
            Permission("Storage", Icons.Default.Star, "Access device storage"),
            Permission("Telephone", Icons.Default.Phone, "Make and manage phone calls"),
            Permission("Contacts", Icons.Default.Person, "Access your contacts"),
            Permission("Microphone", Icons.Default.Settings, "Record audio"),
            Permission("Call Log", Icons.Default.Call, "Access call logs"),
            Permission("SMS", Icons.Default.Email, "Send and view SMS messages"),
            Permission("Camera", Icons.Default.Settings, "Take pictures and record videos"),
            Permission("Location", Icons.Default.LocationOn, "Access device location"),
            Permission("Ignore Battery Optimization", Icons.Default.Build, "Run in background without restrictions"),
            Permission("Enable Notification", Icons.Default.Notifications, "Show notifications"),
            Permission("Display Over Other Apps", Icons.Default.Settings, "Display content over other apps"),
            Permission("Background Location", Icons.Default.Place, "Access location in background"),
            Permission("Installed Application Information", Icons.Default.Info, "Access installed apps information")
        )
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
                            permissions[index] = permissions[index].copy(
                                isGranted = !permissions[index].isGranted
                            )
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
                onClick = onContinue,
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
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    KioskAssistTheme {
        PermissionsScreen()
    }
}