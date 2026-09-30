package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnomalyAlertEntity
import com.example.data.model.InfrastructureAssetEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InfrastructureDao {

    // Assets
    @Query("SELECT * FROM infrastructure_assets ORDER BY healthScore ASC")
    fun getAllAssetsFlow(): Flow<List<InfrastructureAssetEntity>>

    @Query("SELECT * FROM infrastructure_assets WHERE id = :id LIMIT 1")
    suspend fun getAssetById(id: String): InfrastructureAssetEntity?

    @Query("SELECT * FROM infrastructure_assets WHERE id = :id LIMIT 1")
    fun getAssetByIdFlow(id: String): Flow<InfrastructureAssetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<InfrastructureAssetEntity>)

    @Update
    suspend fun updateAsset(asset: InfrastructureAssetEntity)

    // Alerts
    @Query("SELECT * FROM anomaly_alerts ORDER BY timestamp DESC")
    fun getAllAlertsFlow(): Flow<List<AnomalyAlertEntity>>

    @Query("SELECT * FROM anomaly_alerts WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun getAlertsForAssetFlow(assetId: String): Flow<List<AnomalyAlertEntity>>

    @Query("SELECT * FROM anomaly_alerts WHERE id = :id LIMIT 1")
    suspend fun getAlertById(id: String): AnomalyAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AnomalyAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AnomalyAlertEntity>)

    @Query("UPDATE anomaly_alerts SET isResolved = :isResolved WHERE id = :alertId")
    suspend fun updateAlertResolution(alertId: String, isResolved: Boolean)

    // Work Orders
    @Query("SELECT * FROM work_orders ORDER BY createdAt DESC")
    fun getAllWorkOrdersFlow(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE id = :id LIMIT 1")
    suspend fun getWorkOrderById(id: String): WorkOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(workOrder: WorkOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrders(workOrders: List<WorkOrderEntity>)

    @Query("UPDATE work_orders SET status = :status WHERE id = :id")
    suspend fun updateWorkOrderStatus(id: String, status: String)
}
