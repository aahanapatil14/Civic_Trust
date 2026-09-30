package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "infrastructure_assets")
data class InfrastructureAssetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // BRIDGE, FLYOVER, RETAINING_WALL, PILLAR
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val healthScore: Int, // 0 - 100
    val status: String, // NOMINAL, CAUTION, CRITICAL
    val baselineFrequencyHz: Float, // e.g. 28.4 Hz
    val currentFrequencyHz: Float, // e.g. 26.6 Hz
    val lastInspectionTimestamp: Long,
    val activeSensors: Int,
    val pierCount: Int,
    val structuralMaterial: String,
    val yearConstructed: Int,
    val trafficLoadRating: String, // e.g. "IRC Class 70R"
    val riskFactorDescription: String
)
