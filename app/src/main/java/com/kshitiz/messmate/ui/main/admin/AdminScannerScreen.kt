package com.kshitiz.messmate.ui.main.admin

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// weight used at call-site within Column scope
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.kshitiz.messmate.util.QrCodeAnalyzer
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScannerScreen(
    viewModel: AdminViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    var selectedMeal by remember { mutableStateOf(defaultMealByTime()) }
    val canScan: MutableState<Boolean> = remember { mutableStateOf(true) }

    val scanState by viewModel.scanState.collectAsState()

    // React to scan results
    LaunchedEffect(scanState) {
        when (scanState) {
            is Resource.Success -> {
                val msg = (scanState as Resource.Success<String>).data ?: "Success"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                vibrate(context)
                canScan.value = false
                delay(2000)
                viewModel.resetState()
                canScan.value = true
            }
            is Resource.Error -> {
                val error = (scanState as Resource.Error).message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                // Allow scanning again
                canScan.value = true
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Admin QR Scanner") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            MealSelector(selectedMeal = selectedMeal, onSelect = { selectedMeal = it })
            Spacer(Modifier.height(12.dp))

            if (hasCameraPermission) {
                CameraPreview(
                    onQrText = { text ->
                        if (canScan.value) {
                            canScan.value = false // throttle until result
                            val email = text.trim()
                            viewModel.markAttendance(email, selectedMeal)
                        }
                    },
                    enableAnalysis = canScan.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Text("Camera permission is required to scan QR codes.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun MealSelector(selectedMeal: String, onSelect: (String) -> Unit) {
    val meals = listOf("Breakfast", "Lunch", "Snacks", "Dinner")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        meals.forEach { meal ->
            val selected = meal.equals(selectedMeal, ignoreCase = true)
            AssistChip(
                onClick = { onSelect(meal) },
                label = { Text(meal) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun CameraPreview(
    onQrText: (String) -> Unit,
    enableAnalysis: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    // Bind or rebind camera when analysis enabled state changes
    LaunchedEffect(enableAnalysis, lifecycleOwner) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        if (enableAnalysis) {
            analysis.setAnalyzer(ContextCompat.getMainExecutor(context), QrCodeAnalyzer { text ->
                onQrText(text)
            })
        } else {
            analysis.clearAnalyzer()
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                analysis
            )
        } catch (_: Exception) {
        }
    }

    AndroidView(modifier = modifier, factory = { previewView }, update = { })
}

private fun defaultMealByTime(): String {
    // Simple default: Breakfast
    return "Breakfast"
}

private fun vibrate(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            v.vibrate(100)
        }
    } catch (_: Exception) {
    }
}
