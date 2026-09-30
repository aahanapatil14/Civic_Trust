package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HealthCritical
import com.example.ui.theme.PulseTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun CameraCapture(
    onPhotoCaptured: (File) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    targetAssetName: String = "Civic Infrastructure Asset"
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Permission Denied View
    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = PulseTeal,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Camera Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Civic Trust uses the camera to photograph surface cracks, concrete spalling, and shear damage on bridge piers and retaining walls for automated structural verification.",
                        color = Slate200,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseTeal, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_camera_permission_button")
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
        return
    }

    // Photo Preview Confirmation Overlay
    if (capturedPhotoFile != null) {
        val file = capturedPhotoFile!!
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = file,
                contentDescription = "Captured Damage Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Header Overlay
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DAMAGE PHOTO CAPTURED",
                            color = PulseTeal,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = targetAssetName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { capturedPhotoFile = null },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Discard", tint = Color.White)
                    }
                }
            }

            // Bottom Confirmation Buttons
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { capturedPhotoFile = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Slate200),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("retake_photo_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onPhotoCaptured(file)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PulseTeal, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("use_photo_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Main Camera View
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_capture_view")
    ) {
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

        DisposableEffect(Unit) {
            onDispose {
                cameraExecutor.shutdown()
                cameraProvider?.unbindAll()
            }
        }

        // Camera Preview AndroidView
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        provider.unbindAll()
                        activeCamera = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Failed to bind camera: ${e.message}", e)
                        errorMessage = "Camera initialization failed: ${e.message}"
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Targeting HUD Reticle (Concrete crack inspection guide)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val boxSize = w * 0.72f
            val left = cx - (boxSize / 2f)
            val top = cy - (boxSize / 2f)

            // Draw bounding frame corners for targeting cracks
            val cornerLen = 32f
            val stroke = 3.5f

            // Top-left
            drawLine(PulseTeal, Offset(left, top), Offset(left + cornerLen, top), stroke)
            drawLine(PulseTeal, Offset(left, top), Offset(left, top + cornerLen), stroke)

            // Top-right
            drawLine(PulseTeal, Offset(left + boxSize, top), Offset(left + boxSize - cornerLen, top), stroke)
            drawLine(PulseTeal, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLen), stroke)

            // Bottom-left
            drawLine(PulseTeal, Offset(left, top + boxSize), Offset(left + cornerLen, top + boxSize), stroke)
            drawLine(PulseTeal, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLen), stroke)

            // Bottom-right
            drawLine(PulseTeal, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLen, top + boxSize), stroke)
            drawLine(PulseTeal, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLen), stroke)

            // Center crosshair
            drawLine(PulseTeal.copy(alpha = 0.5f), Offset(cx - 16f, cy), Offset(cx + 16f, cy), 1.5f)
            drawLine(PulseTeal.copy(alpha = 0.5f), Offset(cx, cy - 16f), Offset(cx, cy + 16f), 1.5f)
        }

        // Top Controls Bar
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_camera_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FIELD INSPECTION CAMERA",
                        color = PulseTeal,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = targetAssetName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Flash / Torch Toggle
                IconButton(
                    onClick = {
                        isFlashOn = !isFlashOn
                        activeCamera?.cameraControl?.enableTorch(isFlashOn)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isFlashOn) PulseTeal else Color.White
                    )
                }
            }
        }

        // Bottom Capture Controls
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch Lens (Front / Back)
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }

                        cameraProvider?.let { provider ->
                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()
                            val capture = imageCapture ?: return@IconButton
                            try {
                                provider.unbindAll()
                                val preview = Preview.Builder().build()
                                activeCamera = provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraCapture", "Failed to switch lens", e)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Shutter Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.5.dp, PulseTeal, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !isCapturing) {
                            val capture = imageCapture ?: return@clickable
                            isCapturing = true

                            // Haptic vibration feedback
                            try {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                            } catch (e: Exception) {
                                // ignore
                            }

                            val photosDir = File(context.cacheDir, "damaged_infra_photos").apply {
                                if (!exists()) mkdirs()
                            }
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val photoFile = File(photosDir, "INFRA_DMG_${timeStamp}.jpg")

                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            capture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        capturedPhotoFile = photoFile
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        errorMessage = "Capture failed: ${exception.message}"
                                        Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
                                    }
                                }
                            )
                        }
                        .testTag("shutter_button")
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = PulseTeal,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PulseTeal)
                        )
                    }
                }

                // Placeholder space for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Error message snackbar
        if (errorMessage != null) {
            Surface(
                color = HealthCritical,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(errorMessage ?: "", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
