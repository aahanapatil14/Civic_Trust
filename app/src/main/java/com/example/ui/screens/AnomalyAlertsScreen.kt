package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AnomalyAlertEntity
import com.example.ui.components.StatusBadge
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnomalyAlertsScreen(
    viewModel: StructuralPulseViewModel,
    onNavigateToWorkOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAlerts = alerts.filter { alert ->
        when (selectedFilter) {
            "CRITICAL" -> alert.severity == "CRITICAL" && !alert.isResolved
            "HIGH" -> alert.severity == "HIGH" && !alert.isResolved
            "MODERATE" -> alert.severity == "MODERATE" && !alert.isResolved
            "RESOLVED" -> alert.isResolved
            else -> !alert.isResolved
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("anomaly_alerts_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Structural Anomaly Intelligence",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Early warnings detected via continuous acoustic modal tracking",
                            fontSize = 12.sp,
                            color = Slate200
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = HealthCritical,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf(
                        "ALL" to "Active (${alerts.count { !it.isResolved }})",
                        "CRITICAL" to "Critical",
                        "HIGH" to "High",
                        "MODERATE" to "Moderate",
                        "RESOLVED" to "Resolved (${alerts.count { it.isResolved }})"
                    )
                    items(filterOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "CRITICAL") HealthCritical else PulseTeal,
                                selectedLabelColor = if (key == "CRITICAL") Color.White else Color.Black,
                                containerColor = DarkCard,
                                labelColor = Slate200
                            )
                        )
                    }
                }
            }
        }

        if (filteredAlerts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = HealthNominal,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Anomalies in this Category",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "All sensor frequencies are tracking baseline responses.",
                            color = Slate600,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredAlerts, key = { it.id }) { alert ->
                AnomalyAlertCard(
                    alert = alert,
                    onCreateWorkOrder = {
                        viewModel.createWorkOrderFromAlert(alert)
                        onNavigateToWorkOrders()
                    },
                    onToggleResolve = {
                        viewModel.resolveAlert(alert.id, !alert.isResolved)
                    }
                )
            }
        }
    }
}

@Composable
private fun AnomalyAlertCard(
    alert: AnomalyAlertEntity,
    onCreateWorkOrder: () -> Unit,
    onToggleResolve: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedTime = dateFormat.format(Date(alert.timestamp))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (alert.severity) {
                "CRITICAL" -> HealthCritical
                "HIGH" -> HealthCaution
                else -> Slate700
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("anomaly_alert_card_${alert.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.assetName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${alert.pillarId} • $formattedTime",
                        fontSize = 11.sp,
                        color = Slate600,
                        fontFamily = FontFamily.Monospace
                    )
                }
                StatusBadge(status = alert.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.anomalyClassification,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PulseTeal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Frequency Shift & Lifespan Badge Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("FREQUENCY SHIFT", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        "${String.format("%.2f", alert.frequencyShiftHz)} Hz",
                        color = if (alert.frequencyShiftHz < -0.5f) HealthCritical else HealthCaution,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text("AE BURST RATE", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        "${alert.acousticBurstRate} hits/s",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text("LIFESPAN REDUCTION", color = Slate600, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        "-${alert.estimatedLifespanImpactMonths} Months",
                        color = HealthCritical,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.geminiSummary,
                color = Slate200,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Slate800,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("Action: ", color = PulseTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(alert.recommendedAction, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCreateWorkOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = PulseBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dispatch Work Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleResolve,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (alert.isResolved) HealthNominal else Slate700)
                ) {
                    Icon(
                        imageVector = if (alert.isResolved) Icons.Default.Check else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (alert.isResolved) HealthNominal else Slate200,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alert.isResolved) "Resolved" else "Mark Done",
                        color = if (alert.isResolved) HealthNominal else Slate200,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
