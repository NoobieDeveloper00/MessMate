package com.kshitiz.messmate.ui.screens.admin.scanner

import com.kshitiz.messmate.ui.viewmodel.AdminViewModel

// ... Keep existing imports ...
import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kshitiz.messmate.util.QrCodeAnalyzer
import com.kshitiz.messmate.util.Resource
import com.kshitiz.messmate.ui.theme.MessMateTheme
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdminScannerScreen(
    viewModel: AdminViewModel = koinViewModel()
) {
    // Force Dark Theme for Scanner
    MessMateTheme(darkTheme = true) {
        val context = LocalContext.current
        var hasCameraPermission by remember { mutableStateOf(false) }
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasCameraPermission = granted }
        )

        var selectedMeal by remember { mutableStateOf("Breakfast") }
        val canScan = remember { mutableStateOf(true) }
        val scanState by viewModel.scanState.collectAsState()

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
                    Toast.makeText(context, (scanState as Resource.Error).message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                    canScan.value = true
                }
                else -> Unit
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // 1. Camera Layer
                CameraPreview(
                    onQrText = { text ->
                        if (canScan.value) {
                            canScan.value = false
                            viewModel.markAttendance(text.trim(), selectedMeal)
                        }
                    },
                    enableAnalysis = canScan.value,
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Viewfinder Overlay
                ScannerOverlay()
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission required", color = Color.White)
                }
            }

            // 3. UI Controls Layer (Floating on top)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Meal Selector
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Breakfast", "Lunch", "Snacks", "Dinner").forEach { meal ->
                            val isSelected = selectedMeal.equals(meal, ignoreCase = true)
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(containerColor)
                                    .clickable { selectedMeal = meal }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = meal, color = contentColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Bottom Status
                if (!canScan.value) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(1.dp)) // Spacer to keep layout balanced
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scanBoxSize = 280.dp.toPx()
        val left = (size.width - scanBoxSize) / 2
        val top = (size.height - scanBoxSize) / 2

        // Darken outside
        drawRect(color = Color.Black.copy(alpha = 0.5f))
        // Clear center
        drawRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(scanBoxSize, scanBoxSize),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )
        // Draw Border
        drawRect(
            color = Color(0xFF00ADB5), // Verdigris
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(scanBoxSize, scanBoxSize),
            style = Stroke(width = 8f)
        )
    }
}

// ... Keep CameraPreview and vibrate functions exactly as they were ...
@Composable
private fun CameraPreview(
    onQrText: (String) -> Unit,
    enableAnalysis: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { androidx.camera.view.PreviewView(context) }

    LaunchedEffect(enableAnalysis) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        if (enableAnalysis) {
            analysis.setAnalyzer(ContextCompat.getMainExecutor(context), QrCodeAnalyzer { onQrText(it) })
        } else {
            analysis.clearAnalyzer()
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
        } catch (e: Exception) { }
    }
    AndroidView(modifier = modifier, factory = { previewView })
}

private fun vibrate(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.vibrate(100)
    }
}