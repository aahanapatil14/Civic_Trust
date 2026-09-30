package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SensorStreamMode
import com.example.audio.SimulatedStressEvent
import com.example.data.model.AnomalyAlertEntity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.components.CameraCapture
import com.example.ui.components.FftSpectrumVisualizer
import com.example.ui.components.LiveRadarPulse
import com.example.ui.components.StatusBadge
import com.example.ui.components.StructuralHealthMetricCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HealthCaution
import com.example.ui.theme.HealthCritical
import com.example.ui.theme.HealthNominal
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.StructuralPulseViewModel
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AcousticAnalyzerScreen(
    viewModel: StructuralPulseViewModel,
    onNavigateToWorkOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reading by viewModel.liveAcousticReading.collectAsStateWithLifecycle()
    val isStreaming by viewModel.sensorManager.isStreaming.collectAsStateWithLifecycle()
    val streamMode by viewModel.sensorManager.streamMode.collectAsStateWithLifecycle()
    val activeEvent by viewModel.sensorManager.activeStressEvent.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.latestAnalysisResult.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val capturedPhotos by viewModel.capturedPhotos.collectAsStateWithLifecycle()
    val damageLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    var showCamera by remember { mutableStateOf(false) }
    var previewingPhoto by remember { mutableStateOf<java.io.File?>(null) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setStreamMode(SensorStreamMode.LIVE_DEVICE_MIC)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.refreshDamageLocation()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("acoustic_analyzer_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Monitored Target Asset Header
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveRadarPulse(color = PulseTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MONITORED TARGET",
                                color = PulseTeal,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = reading.assetName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Component: ${viewModel.sensorManager.selectedPillarId} • Node: ${reading.sensorNodeId}",
                            fontSize = 12.sp,
                            color = Slate200,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleStreaming() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStreaming) Slate800 else PulseTeal,
                            contentColor = if (isStreaming) Slate200 else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toggle_streaming_button")
                    ) {
                        Icon(
                            imageVector = if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isStreaming) "Pause" else "Stream", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Field Damage Photo Inspection (CameraX) Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("damage_photo_inspection_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VISUAL DAMAGE INSPECTION",
                                color = PulseTeal,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (capturedPhotos.isNotEmpty()) {
                            Text(
                                text = "${capturedPhotos.size} Photo(s) Attached",
                                color = Slate200,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Play Services Fused Location Telemetry Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "GPS Geolocation",
                                tint = PulseTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = damageLocation?.formatCoordinates() ?: "Acquiring GPS fix...",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (damageLocation?.isEstimatedOrFallback == false) {
                                        "Play Services Fused Location (${damageLocation?.formatAccuracy()})"
                                    } else {
                                        "Surveyed Reference Coordinates (Pune Smart City NOC)"
                                    },
                                    color = Slate200,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (viewModel.locationProvider.hasLocationPermission()) {
                                    viewModel.refreshDamageLocation()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("refresh_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Refresh GPS",
                                tint = PulseTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showCamera = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate800,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PulseTeal.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_camera_capture_button")
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Photo of Damaged Infrastructure (CameraX)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (capturedPhotos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(capturedPhotos, key = { it.id }) { photo ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, PulseTeal, RoundedCornerShape(8.dp))
                                        .clickable { previewingPhoto = photo.file }
                                        .testTag("photo_thumbnail_${photo.id}")
                                ) {
                                    AsyncImage(
                                        model = photo.file,
                                        contentDescription = "Damaged infrastructure photo",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stream Source Selector & Stress Event Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Acoustic Ingestion Mode",
                    color = Slate200,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = streamMode == SensorStreamMode.EDGE_IOT_SIMULATOR,
                        onClick = { viewModel.setStreamMode(SensorStreamMode.EDGE_IOT_SIMULATOR) },
                        leadingIcon = { Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("Edge Piezo (10 kHz)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulseTeal,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCard,
                            labelColor = Slate200
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = streamMode == SensorStreamMode.LIVE_DEVICE_MIC,
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.setStreamMode(SensorStreamMode.LIVE_DEVICE_MIC)
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("Phone Microphone", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulseTeal,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCard,
                            labelColor = Slate200
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // If in Edge Simulator, allow triggering civic load simulations
                if (streamMode == SensorStreamMode.EDGE_IOT_SIMULATOR) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Simulate Structural Impulse Event",
                        color = Slate600,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val events = listOf(
                            SimulatedStressEvent.MICRO_FRACTURE_EVENT to "Micro-Fracture",
                            SimulatedStressEvent.HEAVY_TRUCK_PASSAGE to "40T Axle Load",
                            SimulatedStressEvent.NORMAL_FLOW to "Normal Flow"
                        )
                        events.forEach { (ev, label) ->
                            FilterChip(
                                selected = activeEvent == ev,
                                onClick = { viewModel.triggerStressEvent(ev) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (ev == SimulatedStressEvent.MICRO_FRACTURE_EVENT) HealthCritical else PulseBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = DarkCard,
                                    labelColor = Slate200
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Live Oscilloscope Waveform
        item {
            WaveformVisualizer(
                waveform = reading.rawWaveform,
                isStreaming = isStreaming
            )
        }

        // Live Power Spectral Density (FFT)
        item {
            FftSpectrumVisualizer(
                fftBins = reading.fftMagnitudes,
                fundamentalHz = reading.fundamentalFrequencyHz,
                baselineHz = reading.baselineFrequencyHz
            )
        }

        // Physical Telemetry & Fractional Hertz Shift Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACOUSTIC MODAL TELEMETRY",
                    color = Slate200,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val deltaF = reading.frequencyShiftHz
                    val stiffnessLossPercent = max(
                        0f,
                        (1f - (reading.fundamentalFrequencyHz / reading.baselineFrequencyHz) * (reading.fundamentalFrequencyHz / reading.baselineFrequencyHz)) * 100f
                    )

                    StructuralHealthMetricCard(
                        label = "Shift (Δf)",
                        value = "${if (deltaF >= 0) "+" else ""}${String.format("%.2f", deltaF)}",
                        unit = "Hz",
                        delta = "${stiffnessLossPercent.roundToInt()}% Stiffness Loss",
                        isNegativeBad = true,
                        modifier = Modifier.weight(1f)
                    )
                    StructuralHealthMetricCard(
                        label = "AE Burst Rate",
                        value = "${reading.burstRateHitsPerSec}",
                        unit = "hits/s",
                        delta = if (reading.burstRateHitsPerSec > 20) "Micro-cracking" else "Nominal",
                        isNegativeBad = true,
                        modifier = Modifier.weight(1f)
                    )
                    StructuralHealthMetricCard(
                        label = "Damping (ζ)",
                        value = String.format("%.3f", reading.dampingRatio),
                        delta = if (reading.dampingRatio > 0.04f) "Internal Friction" else "Elastic",
                        isNegativeBad = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Gemini AI Diagnosis Button
        item {
            Button(
                onClick = { viewModel.runGeminiAcousticDiagnosis() },
                enabled = !isAnalyzing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseTeal,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_gemini_diagnosis_button")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing Acoustic Spectral Shift with Gemini 3.5...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Gemini Acoustic AI Diagnosis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Gemini Structural Analysis Report Card
        if (analysisResult != null) {
            val result = analysisResult!!
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (result.severityLevel == "CRITICAL") HealthCritical else PulseTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_analysis_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (result.isLiveGeminiCall) PulseTeal.copy(alpha = 0.2f) else Slate800,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (result.isLiveGeminiCall) "GEMINI 3.5 FLASH • TIME-SERIES AI" else "EULER-BERNOULLI BEAM PHYSICS",
                                        color = if (result.isLiveGeminiCall) PulseTeal else Slate200,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            StatusBadge(status = result.severityLevel)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = result.crackClassification,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("STRUCTURAL RISK", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("${result.riskScore}/100", color = if (result.riskScore > 70) HealthCritical else HealthCaution, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("LIFESPAN IMPACT", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("-${result.structuralLifespanReductionMonths} Months", color = HealthCritical, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("NDT VALIDATION", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("UPV Sonic", color = PulseTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Physics & Acoustic Analysis:",
                            color = Slate200,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.physicsDiagnosis,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Acoustic Signatures Detected:",
                            color = Slate200,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        result.acousticSignaturesIdentified.forEach { sig ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = PulseTeal, fontWeight = FontWeight.Bold)
                                Text(sig, color = Slate200, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = HealthCritical.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HealthCritical.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "IMMEDIATE MUNICIPAL SAFETY ACTION:",
                                    color = HealthCritical,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = result.immediateSafetyAction,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val simulatedAlert = AnomalyAlertEntity(
                                    id = "ALT-PMC-${System.currentTimeMillis().toString().takeLast(4)}",
                                    assetId = "PMC-BRG-01",
                                    assetName = reading.assetName,
                                    pillarId = viewModel.sensorManager.selectedPillarId,
                                    timestamp = System.currentTimeMillis(),
                                    severity = result.severityLevel,
                                    riskScore = result.riskScore,
                                    baselineFrequencyHz = reading.baselineFrequencyHz,
                                    observedFrequencyHz = reading.fundamentalFrequencyHz,
                                    frequencyShiftHz = reading.frequencyShiftHz,
                                    acousticBurstRate = reading.burstRateHitsPerSec,
                                    dampingRatio = reading.dampingRatio,
                                    estimatedLifespanImpactMonths = result.structuralLifespanReductionMonths,
                                    anomalyClassification = result.crackClassification,
                                    geminiSummary = result.physicsDiagnosis,
                                    recommendedAction = result.immediateSafetyAction,
                                    isResolved = false
                                )
                                viewModel.createWorkOrderFromAlert(simulatedAlert)
                                onNavigateToWorkOrders()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PulseBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_work_order_button")
                        ) {
                            Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Convert to Scheduled Municipal Work Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCamera) {
        Dialog(
            onDismissRequest = { showCamera = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CameraCapture(
                targetAssetName = "${reading.assetName} (${viewModel.sensorManager.selectedPillarId})",
                onPhotoCaptured = { photoFile ->
                    viewModel.addCapturedPhoto(
                        file = photoFile,
                        assetId = viewModel.selectedAssetId.value ?: "PMC-BRG-01",
                        assetName = reading.assetName,
                        component = viewModel.sensorManager.selectedPillarId,
                        location = damageLocation
                    )
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        }
    }

    val currentPreview = previewingPhoto
    if (currentPreview != null) {
        val attachedPhotoInfo = capturedPhotos.firstOrNull { it.file.absolutePath == currentPreview.absolutePath }
        Dialog(onDismissRequest = { previewingPhoto = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inspection Photo Preview",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { previewingPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = currentPreview,
                        contentDescription = "Photo Preview",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentPreview.name,
                        color = Slate200,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (attachedPhotoInfo?.location != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GPS Geotag: ${attachedPhotoInfo.location.formatCoordinates()} (${attachedPhotoInfo.location.formatAccuracy()})",
                                color = PulseTeal,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
