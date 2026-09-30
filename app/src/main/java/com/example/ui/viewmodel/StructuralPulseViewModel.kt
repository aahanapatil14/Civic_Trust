package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AcousticSensorManager
import com.example.audio.SensorStreamMode
import com.example.audio.SimulatedStressEvent
import com.example.data.gemini.GeminiAcousticAnalyzer
import com.example.data.local.AppDatabase
import com.example.data.model.AcousticReading
import com.example.data.model.AnomalyAlertEntity
import com.example.data.model.GeminiStructuralAnalysisResult
import com.example.data.model.InfrastructureAssetEntity
import com.example.data.model.WorkOrderEntity
import com.example.data.repository.InfrastructureRepository
import com.example.location.DamageGeoLocation
import com.example.location.DamageLocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class StructuralPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = InfrastructureRepository(database.infrastructureDao())
    private val geminiAnalyzer = GeminiAcousticAnalyzer()
    val sensorManager = AcousticSensorManager(application, viewModelScope)
    val locationProvider = DamageLocationProvider(application)

    val allAssets: StateFlow<List<InfrastructureAssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<AnomalyAlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkOrders: StateFlow<List<WorkOrderEntity>> = repository.allWorkOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveAcousticReading: StateFlow<AcousticReading> = sensorManager.currentReading

    private val _selectedAssetId = MutableStateFlow<String?>("PMC-BRG-01")
    val selectedAssetId: StateFlow<String?> = _selectedAssetId.asStateFlow()

    private val _selectedFilterType = MutableStateFlow("ALL")
    val selectedFilterType: StateFlow<String> = _selectedFilterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredAssets: StateFlow<List<InfrastructureAssetEntity>> = combine(
        allAssets,
        _selectedFilterType,
        _searchQuery
    ) { assets, filter, query ->
        assets.filter { asset ->
            val matchesFilter = when (filter) {
                "ALL" -> true
                else -> asset.type.equals(filter, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                asset.name.contains(query, ignoreCase = true) ||
                asset.locationName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _latestAnalysisResult = MutableStateFlow<GeminiStructuralAnalysisResult?>(null)
    val latestAnalysisResult: StateFlow<GeminiStructuralAnalysisResult?> = _latestAnalysisResult.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _capturedPhotos = MutableStateFlow<List<CapturedInfrastructurePhoto>>(emptyList())
    val capturedPhotos: StateFlow<List<CapturedInfrastructurePhoto>> = _capturedPhotos.asStateFlow()

    private val _currentLocation = MutableStateFlow<DamageGeoLocation?>(null)
    val currentLocation: StateFlow<DamageGeoLocation?> = _currentLocation.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            refreshDamageLocation()
        }
    }

    fun refreshDamageLocation() {
        viewModelScope.launch {
            val gpsLocation = locationProvider.getFreshLocation()
            if (gpsLocation != null) {
                _currentLocation.value = gpsLocation
            } else {
                // If device location is unavailable (e.g. initial emulator boot or no GPS fix),
                // supply the accurate surveyed coordinates for the currently selected asset in Pune
                val currentAsset = _selectedAssetId.value?.let { repository.getAssetById(it) }
                if (currentAsset != null) {
                    _currentLocation.value = DamageGeoLocation(
                        latitude = currentAsset.latitude,
                        longitude = currentAsset.longitude,
                        accuracyMeters = 5.0f,
                        isEstimatedOrFallback = true
                    )
                } else {
                    _currentLocation.value = DamageGeoLocation(
                        latitude = 18.52043,
                        longitude = 73.85674,
                        accuracyMeters = 8.0f,
                        isEstimatedOrFallback = true
                    )
                }
            }
        }
    }

    fun selectAsset(assetId: String) {
        _selectedAssetId.value = assetId
        viewModelScope.launch {
            val asset = repository.getAssetById(assetId)
            if (asset != null) {
                sensorManager.selectedAssetName = asset.name
                sensorManager.baselineFrequencyHz = asset.baselineFrequencyHz
                sensorManager.selectedPillarId = when (asset.type) {
                    "BRIDGE" -> "Pier P-04B"
                    "FLYOVER" -> "Cantilever Arm C-03"
                    "RETAINING_WALL" -> "Section B-East"
                    else -> "Base Column 1"
                }
            }
        }
    }

    fun setFilterType(type: String) {
        _selectedFilterType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun runGeminiAcousticDiagnosis() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val currentAsset = _selectedAssetId.value?.let { repository.getAssetById(it) }
                val reading = liveAcousticReading.value

                val result = geminiAnalyzer.analyzeStructuralAcoustics(
                    reading = reading,
                    assetPillar = sensorManager.selectedPillarId,
                    structuralMaterial = currentAsset?.structuralMaterial ?: "Prestressed Reinforced Concrete",
                    yearConstructed = currentAsset?.yearConstructed ?: 1975
                )

                _latestAnalysisResult.value = result

                // If anomaly severity is high or critical, save as an official AnomalyAlert
                if (result.severityLevel == "CRITICAL" || result.severityLevel == "HIGH") {
                    val alert = AnomalyAlertEntity(
                        id = "ALT-PMC-${UUID.randomUUID().toString().take(6).uppercase()}",
                        assetId = currentAsset?.id ?: "PMC-BRG-01",
                        assetName = currentAsset?.name ?: "Bund Garden Bridge",
                        pillarId = sensorManager.selectedPillarId,
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
                    repository.insertAlert(alert)
                }

                _feedbackMessage.value = if (result.isLiveGeminiCall) {
                    "Gemini 3.5 Flash structural analysis complete! Modal shift evaluated."
                } else {
                    "Structural acoustic analysis complete (Euler-Bernoulli Beam Physics Engine)."
                }
            } catch (e: Exception) {
                _feedbackMessage.value = "Analysis failed: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun createWorkOrderFromAlert(alert: AnomalyAlertEntity) {
        viewModelScope.launch {
            val order = repository.createWorkOrderFromAlert(alert)
            _feedbackMessage.value = "Work Order ${order.id} generated & dispatched for ${alert.assetName}!"
        }
    }

    fun updateWorkOrderStatus(workOrderId: String, status: String) {
        viewModelScope.launch {
            repository.updateWorkOrderStatus(workOrderId, status)
            _feedbackMessage.value = "Work Order $workOrderId marked as $status."
        }
    }

    fun resolveAlert(alertId: String, isResolved: Boolean) {
        viewModelScope.launch {
            repository.updateAlertResolution(alertId, isResolved)
            _feedbackMessage.value = if (isResolved) "Alert resolved." else "Alert marked open."
        }
    }

    fun setStreamMode(mode: SensorStreamMode) {
        sensorManager.setStreamMode(mode)
    }

    fun triggerStressEvent(event: SimulatedStressEvent) {
        sensorManager.triggerStressEvent(event)
    }

    fun toggleStreaming() {
        sensorManager.toggleStreaming()
    }

    fun addCapturedPhoto(
        file: java.io.File,
        assetId: String,
        assetName: String,
        component: String,
        location: DamageGeoLocation? = _currentLocation.value
    ) {
        val newPhoto = CapturedInfrastructurePhoto(
            assetId = assetId,
            assetName = assetName,
            componentTarget = component,
            file = file,
            location = location
        )
        _capturedPhotos.value = listOf(newPhoto) + _capturedPhotos.value
        val locText = location?.formatCoordinates()?.let { " at $it" } ?: ""
        _feedbackMessage.value = "Damage photo saved for $assetName ($component)$locText"
    }

    fun deleteCapturedPhoto(photoId: String) {
        _capturedPhotos.value = _capturedPhotos.value.filter { it.id != photoId }
    }
}

data class CapturedInfrastructurePhoto(
    val id: String = java.util.UUID.randomUUID().toString(),
    val assetId: String,
    val assetName: String,
    val componentTarget: String,
    val file: java.io.File,
    val location: DamageGeoLocation? = null,
    val timestamp: Long = System.currentTimeMillis()
)

