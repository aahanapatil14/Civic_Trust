package com.example.data.repository

import com.example.data.local.DefaultData
import com.example.data.local.InfrastructureDao
import com.example.data.model.AnomalyAlertEntity
import com.example.data.model.InfrastructureAssetEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InfrastructureRepository(
    private val dao: InfrastructureDao
) {
    val allAssets: Flow<List<InfrastructureAssetEntity>> = dao.getAllAssetsFlow()
    val allAlerts: Flow<List<AnomalyAlertEntity>> = dao.getAllAlertsFlow()
    val allWorkOrders: Flow<List<WorkOrderEntity>> = dao.getAllWorkOrdersFlow()

    fun getAssetByIdFlow(id: String): Flow<InfrastructureAssetEntity?> = dao.getAssetByIdFlow(id)

    fun getAlertsForAssetFlow(assetId: String): Flow<List<AnomalyAlertEntity>> = dao.getAlertsForAssetFlow(assetId)

    suspend fun ensureInitialized() {
        val currentAssets = dao.getAllAssetsFlow().first()
        if (currentAssets.isEmpty()) {
            dao.insertAssets(DefaultData.getInitialAssets())
            dao.insertAlerts(DefaultData.getInitialAlerts())
            dao.insertWorkOrders(DefaultData.getInitialWorkOrders())
        }
    }

    suspend fun getAssetById(id: String): InfrastructureAssetEntity? = dao.getAssetById(id)

    suspend fun updateAsset(asset: InfrastructureAssetEntity) = dao.updateAsset(asset)

    suspend fun insertAlert(alert: AnomalyAlertEntity) = dao.insertAlert(alert)

    suspend fun updateAlertResolution(alertId: String, isResolved: Boolean) =
        dao.updateAlertResolution(alertId, isResolved)

    suspend fun insertWorkOrder(workOrder: WorkOrderEntity) = dao.insertWorkOrder(workOrder)

    suspend fun updateWorkOrderStatus(workOrderId: String, status: String) =
        dao.updateWorkOrderStatus(workOrderId, status)

    suspend fun createWorkOrderFromAlert(alert: AnomalyAlertEntity): WorkOrderEntity {
        val orderNumber = (100..999).random()
        val newWorkOrder = WorkOrderEntity(
            id = "WO-PMC-2026-$orderNumber",
            assetId = alert.assetId,
            assetName = alert.assetName,
            componentTarget = "${alert.pillarId} Structural Element",
            alertId = alert.id,
            priority = when (alert.severity) {
                "CRITICAL" -> "EMERGENCY"
                "HIGH" -> "HIGH"
                else -> "SCHEDULED"
            },
            status = "DISPATCHED",
            createdAt = System.currentTimeMillis(),
            scheduledDate = "Immediate Dispatch (Within 24 Hours)",
            assignedUnit = "PMC Bridge Engineering Rapid Response Squad",
            contractorName = "National Infrastructure Safety Board - Empaneled Team",
            actionItems = "${alert.recommendedAction}\n• Calibrate acoustic emission piezo sensors\n• Core ultrasonic wave speed verification\n• Submit digital verification report to PMC Smart City NOC",
            estimatedCostInr = when (alert.severity) {
                "CRITICAL" -> 350000L
                "HIGH" -> 180000L
                else -> 75000L
            },
            ircCodeReference = "IRC:SP:35-2019 / IRC:SP:40"
        )
        dao.insertWorkOrder(newWorkOrder)
        dao.updateAlertResolution(alert.id, false)
        return newWorkOrder
    }
}
