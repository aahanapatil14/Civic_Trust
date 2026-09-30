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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.HealthCaution
import com.example.ui.theme.HealthCritical
import com.example.ui.theme.HealthInfo
import com.example.ui.theme.HealthNominal
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status.uppercase()) {
        "CRITICAL", "EMERGENCY" -> HealthCritical to status.uppercase()
        "CAUTION", "HIGH", "WARNING" -> HealthCaution to status.uppercase()
        "MODERATE" -> HealthInfo to status.uppercase()
        else -> HealthNominal to status.uppercase()
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LiveRadarPulse(
    color: Color = PulseTeal,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(24.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = (size.minDimension / 2f) * scale
            )
            drawCircle(
                color = color,
                radius = size.minDimension / 4f
            )
        }
    }
}

@Composable
fun WaveformVisualizer(
    waveform: FloatArray,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f

            // Background Grid Lines
            val gridColor = Slate800
            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            drawLine(
                color = Slate700,
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 1.5f
            )

            if (waveform.isNotEmpty()) {
                val stepX = width / (waveform.size - 1).coerceAtLeast(1)
                val path = Path()

                waveform.forEachIndexed { index, amp ->
                    val x = index * stepX
                    val y = midY - (amp * (midY * 0.85f))
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = if (isStreaming) PulseTeal else Slate600,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isStreaming) {
                LiveRadarPulse(color = PulseTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE SENSOR FEED (10 kHz)",
                    color = PulseTeal,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "FEED PAUSED",
                    color = Slate600,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun FftSpectrumVisualizer(
    fftBins: FloatArray,
    fundamentalHz: Float,
    baselineHz: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "POWER SPECTRAL DENSITY (FFT)",
                    color = Slate200,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Peak: ${String.format("%.1f", fundamentalHz)} Hz (Base: ${String.format("%.1f", baselineHz)} Hz)",
                    color = if (fundamentalHz < baselineHz - 0.5f) HealthCritical else PulseTeal,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val width = size.width
                val height = size.height

                if (fftBins.isNotEmpty()) {
                    val barWidth = (width / fftBins.size) * 0.75f
                    val spacing = (width / fftBins.size) * 0.25f

                    fftBins.forEachIndexed { i, mag ->
                        val barHeight = (mag.coerceIn(0f, 1f) * height).coerceAtLeast(3f)
                        val x = i * (barWidth + spacing)
                        val y = height - barHeight

                        val barColor = when {
                            i == ((fundamentalHz / 60f) * fftBins.size).toInt().coerceIn(0, fftBins.size - 1) -> {
                                if (fundamentalHz < baselineHz - 1.0f) HealthCritical else PulseTeal
                            }
                            mag > 0.6f -> PulseBlue
                            else -> Slate700
                        }

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.4f)),
                                startY = y,
                                endY = height
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0 Hz", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("20 Hz", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("40 Hz", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("60 Hz (Resonant Mode)", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun StructuralHealthMetricCard(
    label: String,
    value: String,
    unit: String = "",
    delta: String? = null,
    isNegativeBad: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label.uppercase(),
                color = Slate600,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        color = Slate200,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if (delta != null) {
                Spacer(modifier = Modifier.height(2.dp))
                val isNegative = delta.startsWith("-")
                val deltaColor = if (isNegativeBad) {
                    if (isNegative) HealthCritical else HealthNominal
                } else {
                    if (isNegative) HealthNominal else HealthCritical
                }
                Text(
                    text = delta,
                    color = deltaColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
