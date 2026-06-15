package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RefreshProfile::class,
        AppRefreshProfile::class,
        OptimizationLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speedDao(): SpeedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speed_mehraan_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.speedDao()
                    // Populate default flagship presets
                    val defaultProfiles = listOf(
                        RefreshProfile(
                            name = "Hyper-Smooth 120Hz Force",
                            description = "Forces standard high and locks peak display refreshing system-wide. Best for high-end flagship responsiveness.",
                            targetRefreshRate = 120,
                            forceHighest = true,
                            disableAdaptive = true,
                            disableOemSwitching = true,
                            cpuGovernor = "performance",
                            gpuBoost = true,
                            thermalThreshold = 48,
                            isSystem = true,
                            isSelected = true
                        ),
                        RefreshProfile(
                            name = "Adaptive Balanced",
                            description = "Standard dynamic adjustment profile. Dynamically scales between low and high based on usage to preserve battery life.",
                            targetRefreshRate = 120,
                            forceHighest = false,
                            disableAdaptive = false,
                            disableOemSwitching = false,
                            cpuGovernor = "interactive",
                            gpuBoost = false,
                            thermalThreshold = 43,
                            isSystem = true,
                            isSelected = false
                        ),
                        RefreshProfile(
                            name = "Eco Preservation (60Hz)",
                            description = "Forces screens to 60Hz or lower. Drops system governor ticks to conserve battery. Max endurance preset.",
                            targetRefreshRate = 60,
                            forceHighest = false,
                            disableAdaptive = true,
                            disableOemSwitching = true,
                            cpuGovernor = "powersave",
                            gpuBoost = false,
                            thermalThreshold = 38,
                            isSystem = true,
                            isSelected = false
                        ),
                        RefreshProfile(
                            name = "Peak Gaming Center Lock",
                            description = "Optimized for continuous FPS flow. Locks displays at maximum (120Hz/144Hz) with heavy gaming background control.",
                            targetRefreshRate = 144,
                            forceHighest = true,
                            disableAdaptive = true,
                            disableOemSwitching = true,
                            gamingLock = true,
                            cpuGovernor = "performance",
                            gpuBoost = true,
                            thermalThreshold = 50,
                            isSystem = true,
                            isSelected = false
                        )
                    )
                    
                    for (profile in defaultProfiles) {
                        dao.insertProfile(profile)
                    }

                    // Add default logging
                    dao.insertLog(
                        OptimizationLog(
                            actionName = "System Initialized",
                            description = "Speed flagship kernel controller loaded completely by Mehraann.",
                            resultStatus = "Success"
                        )
                    )
                }
            }
        }
    }
}
