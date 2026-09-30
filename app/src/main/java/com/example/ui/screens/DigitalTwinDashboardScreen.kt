package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.InfrastructureAssetEntity
import com.example.ui.components.PuneDigitalTwinMap
import com.example.ui.components.StatusBadge
import com.example.ui.components.StructuralHealthMetricCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalTwinDashboardScreen(
    viewModel: StructuralPulseViewModel,
    onNavigateToAnalyzer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.filteredAssets.collectAsStateWithLifecycle()
    val allAssets by viewModel.allAssets.collectAsStateWithLifecycle()
    val selectedAssetId by viewModel.selectedAssetId.collectAsStateWithLifecycle()
    val filterType by viewModel.selectedFilterType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val selectedAsset = allAssets.firstOrNull { it.id == selectedAssetId } ?: allAssets.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("digital_twin_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bridge_twin_hero),
                    contentDescription = "Pune Infrastructure Digital Twin",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xEE0B111E)),
                                startY = 40f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PulseTeal,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "GDG CLOUD PUNE • TRACK 1: DPI & GOVERNANCE",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Civic Trust",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Autonomous Acoustic Anomaly & Micro-Fracture Early Warning Copilot",
                        color = Slate200,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Civic Health Integrity KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StructuralHealthMetricCard(
                    label = "Civic Health",
                    value = "94.2%",
                    delta = "Nominal Target",
                    isNegativeBad = false,
                    modifier = Modifier.weight(1f)
                )
                StructuralHealthMetricCard(
                    label = "Active Assets",
                    value = "${allAssets.size}",
                    unit = "Sites",
                    delta = "48 IoT Nodes",
                    isNegativeBad = false,
                    modifier = Modifier.weight(1f)
                )
                StructuralHealthMetricCard(
                    label = "Critical Alert",
                    value = "1",
                    unit = "Active",
                    delta = "Pier P-04B",
                    isNegativeBad = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Geospatial Digital Twin Map
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Municipal Digital Twin Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap Pin to Select",
                        fontSize = 11.sp,
                        color = PulseTeal,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                PuneDigitalTwinMap(
                    assets = allAssets,
                    selectedAssetId = selectedAssetId,
                    onAssetSelected = { id -> viewModel.selectAsset(id) }
                )
            }
        }

        // Selected Asset Telemetry Spotlight
        if (selectedAsset != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (selectedAsset.status == "CRITICAL") HealthCritical else PulseTeal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_asset_spotlight")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedAsset.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Slate600,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = selectedAsset.locationName,
                                        fontSize = 11.sp,
                                        color = Slate200
                                    )
                                }
                            }
                            StatusBadge(status = selectedAsset.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modal Frequency Telemetry Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BASELINE (f₀)", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    "${String.format("%.1f", selectedAsset.baselineFrequencyHz)} Hz",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CURRENT (f)", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    "${String.format("%.1f", selectedAsset.currentFrequencyHz)} Hz",
                                    color = if (selectedAsset.currentFrequencyHz < selectedAsset.baselineFrequencyHz - 0.5f) HealthCritical else PulseTeal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val delta = selectedAsset.currentFrequencyHz - selectedAsset.baselineFrequencyHz
                                Text("SHIFT (Δf)", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    "${if (delta >= 0) "+" else ""}${String.format("%.2f", delta)} Hz",
                                    color = if (delta < -0.5f) HealthCritical else HealthNominal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HEALTH", color = Slate600, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    "${selectedAsset.healthScore}/100",
                                    color = if (selectedAsset.healthScore < 75) HealthCritical else HealthNominal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = selectedAsset.riskFactorDescription,
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateToAnalyzer(selectedAsset.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAsset.status == "CRITICAL") HealthCritical else PulseTeal,
                                contentColor = if (selectedAsset.status == "CRITICAL") Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inspect_acoustic_stream_button")
                        ) {
                            Icon(imageVector = Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inspect Acoustic Stream & Run Gemini Diagnosis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Filter and Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search bridge, flyover or pier...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate600) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseTeal,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("asset_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf(
                        "ALL" to "All Assets (${allAssets.size})",
                        "BRIDGE" to "Bridges",
                        "FLYOVER" to "Flyovers",
                        "RETAINING_WALL" to "Retaining Walls"
                    )
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = filterType == key,
                            onClick = { viewModel.setFilterType(key) },
                            label = { Text(label, fontSize = 12.sp) },
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

        // Infrastructure Asset Cards List
        items(assets, key = { it.id }) { asset ->
            AssetCard(
                asset = asset,
                isSelected = asset.id == selectedAssetId,
                onClick = {
                    viewModel.selectAsset(asset.id)
                },
                onAnalyzeClick = {
                    viewModel.selectAsset(asset.id)
                    onNavigateToAnalyzer(asset.id)
                }
            )
        }
    }
}

@Composable
private fun AssetCard(
    asset: InfrastructureAssetEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAnalyzeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PulseTeal else Slate700
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("asset_card_${asset.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${asset.type.replace("_", " ")} • Built ${asset.yearConstructed}",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
                StatusBadge(status = asset.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Sensors, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${asset.activeSensors} Piezo Nodes", fontSize = 11.sp, color = Slate200, fontFamily = FontFamily.Monospace)
                }

                val delta = asset.currentFrequencyHz - asset.baselineFrequencyHz
                Text(
                    text = "Δf: ${if (delta >= 0) "+" else ""}${String.format("%.2f", delta)} Hz",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (delta < -0.5f) HealthCritical else HealthNominal
                )

                Text(
                    text = "Health: ${asset.healthScore}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (asset.healthScore < 75) HealthCritical else HealthNominal
                )
            }
        }
    }
}
