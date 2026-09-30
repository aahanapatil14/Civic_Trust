package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey val id: String, // e.g. "WO-PMC-2026-042"
    val assetId: String,
    val assetName: String,
    val componentTarget: String, // e.g. "Pier P-04B Substructure"
    val alertId: String?,
    val priority: String, // EMERGENCY, HIGH, SCHEDULED
    val status: String, // SCHEDULED, DISPATCHED, IN_PROGRESS, RESOLVED
    val createdAt: Long,
    val scheduledDate: String,
    val assignedUnit: String, // e.g. "PMC Bridge Engineering Division / Sector 2"
    val contractorName: String,
    val actionItems: String, // Comma or newline separated steps
    val estimatedCostInr: Long,
    val ircCodeReference: String // e.g. "IRC:SP:35-2019"
)
