package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WorkOrderEntity
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WorkOrdersScreen(
    viewModel: StructuralPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workOrders by viewModel.allWorkOrders.collectAsStateWithLifecycle()
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = workOrders.filter { order ->
        when (selectedStatusFilter) {
            "DISPATCHED" -> order.status == "DISPATCHED"
            "IN_PROGRESS" -> order.status == "IN_PROGRESS"
            "SCHEDULED" -> order.status == "SCHEDULED"
            "RESOLVED" -> order.status == "RESOLVED"
            else -> true
        }
    }

    val totalBudget = workOrders.sumOf { it.estimatedCostInr }
    val inrFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("work_orders_screen"),
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
                            text = "Municipal Maintenance Orders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "PMC Bridge Engineering Wing • Field Dispatch System",
                            fontSize = 12.sp,
                            color = Slate200
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = PulseTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Bar
                Surface(
                    color = DarkCard,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE ORDERS", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                "${workOrders.count { it.status != "RESOLVED" }}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ALLOCATED BUDGET", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                inrFormatter.format(totalBudget),
                                color = PulseTeal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STANDARD", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text("IRC:SP:35", color = Slate200, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statusOptions = listOf(
                        "ALL" to "All (${workOrders.size})",
                        "DISPATCHED" to "Dispatched",
                        "IN_PROGRESS" to "In Progress",
                        "SCHEDULED" to "Scheduled",
                        "RESOLVED" to "Completed"
                    )
                    items(statusOptions) { (key, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == key,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseTeal,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = Slate200
                            )
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
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
                            text = "No Work Orders Found",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                WorkOrderCard(
                    order = order,
                    onStatusChange = { newStatus ->
                        viewModel.updateWorkOrderStatus(order.id, newStatus)
                    },
                    onShareOrder = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "PMC WORK ORDER DISPATCH: ${order.id}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
                                ==========================================
                                PUNE MUNICIPAL CORPORATION (PMC)
                                STRUCTURAL HEALTH MONITORING - WORK ORDER
                                ==========================================
                                ORDER ID: ${order.id}
                                PRIORITY: ${order.priority}
                                STATUS: ${order.status}
                                ASSET: ${order.assetName}
                                TARGET: ${order.componentTarget}
                                ASSIGNED DIVISION: ${order.assignedUnit}
                                CONTRACTOR: ${order.contractorName}
                                ESTIMATED BUDGET: ₹${order.estimatedCostInr}
                                CODE STANDARD: ${order.ircCodeReference}
                                
                                SCOPE OF WORK & ACTION ITEMS:
                                ${order.actionItems}
                                
                                Generated via Civic Trust - Autonomous Civic Infrastructure Copilot
                                GDG Cloud Pune Code for Communities Hackathon
                                ==========================================
                                """.trimIndent()
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Municipal Work Order"))
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkOrderCard(
    order: WorkOrderEntity,
    onStatusChange: (String) -> Unit,
    onShareOrder: () -> Unit
) {
    val inrFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (order.priority) {
                "EMERGENCY" -> HealthCritical
                "HIGH" -> HealthCaution
                else -> Slate700
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("work_order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PulseTeal,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = order.assetName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = order.componentTarget,
                        fontSize = 12.sp,
                        color = Slate200
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = order.priority)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onShareOrder,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Slate200, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Schedule:", color = Slate600, fontSize = 11.sp)
                    Text(order.scheduledDate, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Assigned Unit:", color = Slate600, fontSize = 11.sp)
                    Text(order.assignedUnit, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Contractor / Agency:", color = Slate600, fontSize = 11.sp)
                    Text(order.contractorName, color = Slate200, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Budget:", color = Slate600, fontSize = 11.sp)
                    Text(inrFormatter.format(order.estimatedCostInr), color = PulseTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Maintenance Scope & NDT Protocol:",
                color = Slate200,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.actionItems,
                color = Slate200,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status Stepper Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val statuses = listOf(
                    "DISPATCHED" to "Dispatched",
                    "IN_PROGRESS" to "In Progress",
                    "RESOLVED" to "Completed"
                )
                statuses.forEach { (statKey, statLabel) ->
                    val isActive = order.status == statKey
                    Button(
                        onClick = { onStatusChange(statKey) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) PulseTeal else Slate800,
                            contentColor = if (isActive) Color.Black else Slate200
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = statLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
