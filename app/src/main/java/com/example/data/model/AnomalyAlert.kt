package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anomaly_alerts")
data class AnomalyAlertEntity(
    @PrimaryKey val id: String,
    val assetId: String,
    val assetName: String,
    val pillarId: String, // e.g. "Pier P-04B"
    val timestamp: Long,
    val severity: String, // CRITICAL, HIGH, MODERATE, LOW
    val riskScore: Int, // 0 - 100
    val baselineFrequencyHz: Float,
    val observedFrequencyHz: Float,
    val frequencyShiftHz: Float, // Negative indicates loss of structural stiffness
    val acousticBurstRate: Int, // Hits per second
    val dampingRatio: Float, // Structural modal damping (higher = cracked dissipation)
    val estimatedLifespanImpactMonths: Int,
    val anomalyClassification: String, // Micro-fracture, Rebar Delamination, Foundation Scour
    val geminiSummary: String,
    val recommendedAction: String,
    val isResolved: Boolean = false,
    val workOrderId: String? = null
)
