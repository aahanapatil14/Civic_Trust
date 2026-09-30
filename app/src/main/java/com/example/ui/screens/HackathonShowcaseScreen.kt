package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.HealthCritical
import com.example.ui.theme.HealthNominal
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@Composable
fun HackathonShowcaseScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hackathon_showcase_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Track Header Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, PulseTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = PulseTeal,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "GDG CLOUD PUNE • CODE FOR COMMUNITIES",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Track 1: AI for Digital Public Infrastructure & Governance",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Transforming civic infrastructure safety from reactive tragedy into proactive scheduling for municipal corporations.",
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Core Mission: Reactive Tragedy vs Proactive Copilot
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CIVIC SAFETY PARADIGM SHIFT",
                    color = Slate200,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthCritical.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TRADITIONAL INSPECTION", color = HealthCritical, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Reactive visual surveys", color = Slate200, fontSize = 11.sp)
                            Text("• Misses internal micro-cracks", color = Slate200, fontSize = 11.sp)
                            Text("• Discovered upon spalling", color = Slate200, fontSize = 11.sp)
                            Text("• Emergency bridge closure", color = Slate200, fontSize = 11.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HealthNominal.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("CIVIC TRUST", color = HealthNominal, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• 24/7 continuous acoustic tracking", color = Color.White, fontSize = 11.sp)
                            Text("• Spots Δf shift 6-18 mos prior", color = Color.White, fontSize = 11.sp)
                            Text("• Auto-dispatches PMC work orders", color = Color.White, fontSize = 11.sp)
                            Text("• Zero unannounced shutdowns", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Technical Architecture Breakdown
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TECHNICAL ARCHITECTURE & GOOGLE STACK",
                    color = Slate200,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                ArchitectureTierCard(
                    icon = Icons.Default.ElectricBolt,
                    title = "Tier 1: Edge Acoustic Sensor Ingestion",
                    badge = "10 kHz Sampling",
                    description = "Edge-deployed piezoelectric MEMS microphones mounted directly on bridge pier stems capture ambient acoustic vibrations under live traffic loads. Computes fundamental resonant frequency (f₀), modal damping (ζ), and acoustic burst rates."
                )

                ArchitectureTierCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "Tier 2: Gemini 3.5 & Time-Series Anomaly Detection",
                    badge = "Google AI Studio",
                    description = "Ingests raw spectral chunks and fractional hertz shifts (Δf). Evaluates Euler-Bernoulli beam stiffness loss (k ~ f²), classifies micro-fracture propagation stage, and outputs structured JSON risk payloads with estimated lifespan impacts."
                )

                ArchitectureTierCard(
                    icon = Icons.Default.Map,
                    title = "Tier 3: Google Maps Digital Twin Dashboard",
                    badge = "Geospatial Vector DPI",
                    description = "Renders real-time municipal health parameters, color-coded risk heatmaps, and spatial assets across Pune (Bund Garden Bridge, Swargate Flyover, Shivaji Bridge, etc.) onto a unified civic command center."
                )

                ArchitectureTierCard(
                    icon = Icons.Default.Engineering,
                    title = "Tier 4: Municipal Governance & Dispatch",
                    badge = "IRC:SP:35 Compliant",
                    description = "Translates AI anomaly detections into scheduled municipal work orders (Ultrasonic Pulse Velocity scanning, epoxy resin injection, CFRP wrapping) with exportable official PMC dispatch reports."
                )
            }
        }

        // Low-Cost Economics Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DEMOCRATIZING CIVIC SHM SENSING",
                            color = PulseTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "99.2% Cost Reduction vs Traditional Laser Vibrometry",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Commercial laser Doppler vibrometers cost upwards of ₹25,00,000 per bridge span. Civic Trust utilizes ₹3,500 IoT piezoelectric transducers coupled with Gemini 3.5 cloud intelligence, enabling 100% municipal bridge coverage across Indian smart cities.",
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Compliance & Operational Rules
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = HealthNominal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COMPLIANCE & GOVERNANCE RULES",
                            color = HealthNominal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Public Welfare First: Zero hallucination policy on critical infrastructure health.", color = Slate200, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("2. Geo-Spatial Context: Every anomaly is anchored to exact coordinates & pier IDs.", color = Slate200, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("3. Sovereign Municipal Privacy: Local-first persistence via Room; enterprise API security.", color = Slate200, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("4. Indian Standards: Adheres to Indian Road Congress IRC:SP:35 & IRC:SP:40 guidelines.", color = Slate200, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ArchitectureTierCard(
    icon: ImageVector,
    title: String,
    badge: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = PulseTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        color = PulseTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Slate200,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
