package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "refresh_profiles")
data class RefreshProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val targetRefreshRate: Int,
    val forceHighest: Boolean = false,
    val disableAdaptive: Boolean = false,
    val disableOemSwitching: Boolean = false,
    val gamingLock: Boolean = false,
    val cpuGovernor: String = "interactive", // performance, interactive, schedutil, powersave
    val gpuBoost: Boolean = false,
    val thermalThreshold: Int = 45, // Celsius
    val isSystem: Boolean = false,
    val isSelected: Boolean = false
)

@Entity(tableName = "app_refresh_profiles")
data class AppRefreshProfile(
    @PrimaryKey val packageName: String,
    val appName: String,
    val refreshRate: Int,
    val limitBackground: Boolean = false,
    val gameModeEnabled: Boolean = true
)

@Entity(tableName = "optimization_logs")
data class OptimizationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resultStatus: String // Success, Failed, Info
)
