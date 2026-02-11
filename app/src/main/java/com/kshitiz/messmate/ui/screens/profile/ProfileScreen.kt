package com.kshitiz.messmate.ui.screens.profile

import com.kshitiz.messmate.ui.viewmodel.ProfileViewModel
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.util.Resource

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val profileUiState by viewModel.uiState.collectAsState()
    val state = profileUiState.profileState

    val editableName = remember { mutableStateOf("") }
    val editableFav = remember { mutableStateOf("") }
    // Add a flag to track if data is loaded initially
    var isDataLoaded by remember { mutableStateOf(false) }

    // Sync state
    LaunchedEffect(state) {
        val data = (state as? Resource.Success<User>)?.data
        if (data != null) {
            // ONLY set the text fields if we haven't loaded data yet
            // This prevents the field from resetting while you type if a background update happens
            if (!isDataLoaded) {
                editableName.value = data.name
                editableFav.value = data.favouriteMeal
                isDataLoaded = true
            }
        }

        if (state is Resource.Error) {
            Toast.makeText(context, (state as Resource.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    val profile = (state as? Resource.Success<User>)?.data

    // Background Gradient for Header
    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerGradient)
    ) {
        // --- 1. Header Title (Matched Size & Padding) ---
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Thin, // Matches Attendance/Menu style
            fontSize = 40.sp,             // Matches Attendance/Menu size
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)     // Consistent top padding
        )

        // --- 2. Content Sheet (Matched Height) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp) // Consistent sheet start height
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Name & Email
            Text(
                text = profile?.name?.ifBlank { "Student" } ?: "Student",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = profile?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Details Card ---
            EditDetailsCard(
                editableName = editableName,
                editableFav = editableFav,
                onSave = { viewModel.saveProfile(editableName.value.trim(), editableFav.value.trim()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Logout Confirmation Dialog ---
            var showLogoutDialog by remember { mutableStateOf(false) }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Log Out") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("No") }
                    }
                )
            }

            // --- Logout Button ---
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }

            // Padding for the floating navigation bar
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}