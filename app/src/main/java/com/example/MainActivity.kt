package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LiveRadarPulse
import com.example.ui.screens.AcousticAnalyzerScreen
import com.example.ui.screens.AnomalyAlertsScreen
import com.example.ui.screens.DigitalTwinDashboardScreen
import com.example.ui.screens.HackathonShowcaseScreen
import com.example.ui.screens.WorkOrdersScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HealthCritical
import com.example.ui.theme.PulseTeal
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.StructuralPulseTheme
import com.example.ui.viewmodel.StructuralPulseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StructuralPulseTheme(darkTheme = true) {
                StructuralPulseApp()
            }
        }
    }
}

enum class AppTab(val title: String) {
    DIGITAL_TWIN("Digital Twin"),
    ACOUSTIC_AI("Acoustic Stream"),
    ALERTS("Anomalies"),
    WORK_ORDERS("Work Orders"),
    TRACK_INFO("Track 1")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuralPulseApp(
    viewModel: StructuralPulseViewModel = viewModel()
) {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val allAlerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val unresolvedAlertCount = allAlerts.count { !it.isResolved && (it.severity == "CRITICAL" || it.severity == "HIGH") }

    // Back button behavior for custom navigation state: pops back to Digital Twin screen
    if (currentTab != 0) {
        BackHandler {
            currentTab = 0
        }
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiveRadarPulse(color = PulseTeal, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CIVIC TRUST",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "PMC Infrastructure Health NOC",
                                fontSize = 10.sp,
                                color = Slate200
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("app_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Project Info",
                            tint = PulseTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Digital Twin") },
                    label = { Text("Digital Twin", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PulseTeal,
                        indicatorColor = PulseTeal,
                        unselectedIconColor = Slate600,
                        unselectedTextColor = Slate600
                    ),
                    modifier = Modifier.testTag("nav_digital_twin")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Sensors, contentDescription = "Acoustic AI") },
                    label = { Text("Acoustic AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PulseTeal,
                        indicatorColor = PulseTeal,
                        unselectedIconColor = Slate600,
                        unselectedTextColor = Slate600
                    ),
                    modifier = Modifier.testTag("nav_acoustic_ai")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        if (unresolvedAlertCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = HealthCritical,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unresolvedAlertCount")
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Anomalies")
                            }
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = "Anomalies")
                        }
                    },
                    label = { Text("Anomalies", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PulseTeal,
                        indicatorColor = PulseTeal,
                        unselectedIconColor = Slate600,
                        unselectedTextColor = Slate600
                    ),
                    modifier = Modifier.testTag("nav_anomalies")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Work Orders") },
                    label = { Text("Work Orders", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PulseTeal,
                        indicatorColor = PulseTeal,
                        unselectedIconColor = Slate600,
                        unselectedTextColor = Slate600
                    ),
                    modifier = Modifier.testTag("nav_work_orders")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Track 1") },
                    label = { Text("Track 1", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PulseTeal,
                        indicatorColor = PulseTeal,
                        unselectedIconColor = Slate600,
                        unselectedTextColor = Slate600
                    ),
                    modifier = Modifier.testTag("nav_hackathon_track")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DigitalTwinDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAnalyzer = { assetId ->
                        viewModel.selectAsset(assetId)
                        currentTab = 1
                    }
                )
                1 -> AcousticAnalyzerScreen(
                    viewModel = viewModel,
                    onNavigateToWorkOrders = { currentTab = 3 }
                )
                2 -> AnomalyAlertsScreen(
                    viewModel = viewModel,
                    onNavigateToWorkOrders = { currentTab = 3 }
                )
                3 -> WorkOrdersScreen(
                    viewModel = viewModel
                )
                4 -> HackathonShowcaseScreen()
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = DarkCard,
            title = {
                Text(
                    text = "Civic Trust",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Built for GDG Cloud Pune \"Code for Communities\" Hackathon.",
                        color = PulseTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track 1: AI for Digital Public Infrastructure & Governance.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Purpose: Detects internal structural micro-fractures in bridges, flyovers, and retaining walls months before visual appearance by tracking fractional hertz frequency shifts and acoustic emissions under traffic vibration.",
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Google Stack: Gemini 3.5 Flash / Vertex AI for acoustic time-series spectral parsing, Google Maps Digital Twin visualization, and Android Room local persistence.",
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PulseTeal, contentColor = Color.Black)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}
