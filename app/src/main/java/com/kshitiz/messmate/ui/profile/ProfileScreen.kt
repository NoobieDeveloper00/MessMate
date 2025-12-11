package com.kshitiz.messmate.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kshitiz.messmate.util.Resource

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val state by viewModel.profileState.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) viewModel.uploadPhoto(uri)
    }

    val editableName = remember { mutableStateOf("") }
    val editableFav = remember { mutableStateOf("") }

    LaunchedEffect(state) {
        val data = (state as? Resource.Success<UserProfile>)?.data
        if (data != null) {
            if (editableName.value.isEmpty()) editableName.value = data.name
            if (editableFav.value.isEmpty()) editableFav.value = data.favouriteMeal
        }
        if (state is Resource.Error) {
            android.widget.Toast.makeText(
                context,
                (state as Resource.Error).message,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Thin,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Avatar
        val profile = (state as? Resource.Success<UserProfile>)?.data
        val initial = (profile?.name?.firstOrNull()?.uppercase()
            ?: profile?.email?.firstOrNull()?.uppercase()) ?: "?"

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            if (!profile?.photoUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = profile!!.photoUrl),
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { pickImageLauncher.launch("image/*") }) { Text("Change Photo") }

        Spacer(Modifier.height(24.dp))

        // Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Name", fontWeight = FontWeight.SemiBold)
            Text(text = profile?.name.orEmpty())
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Email", fontWeight = FontWeight.SemiBold)
            Text(text = profile?.email.orEmpty())
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Favourite Meal", fontWeight = FontWeight.SemiBold)
            Text(text = profile?.favouriteMeal.orEmpty())
        }

        Spacer(Modifier.height(24.dp))
        // Edit form
        OutlinedTextField(
            value = editableName.value,
            onValueChange = { editableName.value = it },
            label = { Text("Edit Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = editableFav.value,
            onValueChange = { editableFav.value = it },
            label = { Text("Edit Favourite Meal (dish)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.saveProfile(editableName.value.trim(), editableFav.value.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Profile") }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "You can edit your profile above.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
