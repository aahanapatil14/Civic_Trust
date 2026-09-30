package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AnomalyAlertEntity
import com.example.data.model.InfrastructureAssetEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        InfrastructureAssetEntity::class,
        AnomalyAlertEntity::class,
        WorkOrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun infrastructureDao(): InfrastructureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "structural_pulse_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database.infrastructureDao())
                    }
                }
            }

            suspend fun populateInitialDatabase(dao: InfrastructureDao) {
                dao.insertAssets(DefaultData.getInitialAssets())
                dao.insertAlerts(DefaultData.getInitialAlerts())
                dao.insertWorkOrders(DefaultData.getInitialWorkOrders())
            }
        }
    }
}
