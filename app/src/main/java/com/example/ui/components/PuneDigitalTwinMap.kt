package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InfrastructureAssetEntity
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
import com.example.ui.theme.Slate900
import kotlin.math.hypot

@Composable
fun PuneDigitalTwinMap(
    assets: List<InfrastructureAssetEntity>,
    selectedAssetId: String?,
    onAssetSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Geospatial bounds for Pune Core Urban Infrastructure (Lat: 18.49 .. 18.57, Lng: 73.76 .. 73.90)
    val minLat = 18.495
    val maxLat = 18.568
    val minLng = 73.765
    val maxLng = 73.895

    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(DarkCard, RoundedCornerShape(16.dp))
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(assets) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height

                        var closestAsset: InfrastructureAssetEntity? = null
                        var minDistance = Float.MAX_VALUE

                        assets.forEach { asset ->
                            val normX = ((asset.longitude - minLng) / (maxLng - minLng)).toFloat()
                            val normY = (1.0f - ((asset.latitude - minLat) / (maxLat - minLat))).toFloat()

                            val px = normX * w
                            val py = normY * h

                            val dist = hypot(tapOffset.x - px, tapOffset.y - py)
                            if (dist < 48f && dist < minDistance) {
                                minDistance = dist
                                closestAsset = asset
                            }
                        }

                        closestAsset?.let { onAssetSelected(it.id) }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle digital twin coordinate grid
            for (gx in 1..7) {
                val x = w * (gx / 8f)
                drawLine(
                    color = Slate800,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }
            for (gy in 1..5) {
                val y = h * (gy / 6f)
                drawLine(
                    color = Slate800,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            // 2. Draw Mula-Mutha River Ribbon (Geospatially mapped curve)
            val riverPath = Path().apply {
                // Mutha River coming from South-West (Khadakwasla side)
                moveTo(w * 0.15f, h * 0.85f)
                cubicTo(
                    w * 0.35f, h * 0.65f,
                    w * 0.50f, h * 0.55f,
                    w * 0.60f, h * 0.40f // Confluence at Sangam
                )
                // Mula River joining from North-West (Aundh / Kirkee)
                // Mutha then flows East past Bund Garden
                cubicTo(
                    w * 0.70f, h * 0.35f,
                    w * 0.82f, h * 0.32f,
                    w * 0.98f, h * 0.30f
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF1E3A8A).copy(alpha = 0.55f),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            drawPath(
                path = riverPath,
                color = PulseTeal.copy(alpha = 0.25f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // 3. Draw Highway Artery Paths (NH-48 Bypass & Inner Ring)
            val nh48Path = Path().apply {
                moveTo(w * 0.08f, h * 0.12f)
                lineTo(w * 0.12f, h * 0.55f) // Chandani Chowk
                lineTo(w * 0.22f, h * 0.92f)
            }
            drawPath(
                path = nh48Path,
                color = Slate700,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // 4. Render Infrastructure Asset Pins & Pulse Indicators
            assets.forEach { asset ->
                val normX = ((asset.longitude - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.08f, 0.92f)
                val normY = (1.0f - ((asset.latitude - minLat) / (maxLat - minLat))).toFloat().coerceIn(0.10f, 0.90f)

                val px = normX * w
                val py = normY * h

                val statusColor = when (asset.status.uppercase()) {
                    "CRITICAL" -> HealthCritical
                    "CAUTION" -> HealthCaution
                    else -> HealthNominal
                }

                val isSelected = asset.id == selectedAssetId

                // Draw radar ring if Critical or Selected
                if (asset.status == "CRITICAL" || isSelected) {
                    drawCircle(
                        color = statusColor.copy(alpha = pulseAlpha),
                        radius = 24f * pulseScale,
                        center = Offset(px, py)
                    )
                }

                // Outer halo
                drawCircle(
                    color = if (isSelected) PulseTeal else statusColor.copy(alpha = 0.3f),
                    radius = if (isSelected) 14f else 9f,
                    center = Offset(px, py)
                )

                // Core pin
                drawCircle(
                    color = statusColor,
                    radius = if (isSelected) 8f else 5.5f,
                    center = Offset(px, py)
                )

                // White pinpoint center
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(px, py)
                )
            }
        }

        // Header overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Slate900.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(7.dp).background(PulseTeal, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PUNE MUNICIPAL DIGITAL TWIN (PMC / SMART CITIES)",
                        color = Slate200,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Map Legend Bottom Overlay
        Surface(
            color = Slate900.copy(alpha = 0.90f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(HealthNominal, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Safe", color = Slate200, fontSize = 9.sp, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(6.dp).background(HealthCaution, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modal Drift", color = Slate200, fontSize = 9.sp, fontFamily = FontFamily.Monospace)

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(6.dp).background(HealthCritical, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Critical Anomaly", color = HealthCritical, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
