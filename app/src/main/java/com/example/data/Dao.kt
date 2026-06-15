package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedDao {
    @Query("SELECT * FROM refresh_profiles ORDER BY isSystem DESC, name ASC")
    fun getAllProfiles(): Flow<List<RefreshProfile>>

    @Query("SELECT * FROM refresh_profiles WHERE isSelected = 1 LIMIT 1")
    fun getActiveProfile(): Flow<RefreshProfile?>

    @Query("SELECT * FROM refresh_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): RefreshProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: RefreshProfile): Long

    @Update
    suspend fun updateProfile(profile: RefreshProfile)

    @Delete
    suspend fun deleteProfile(profile: RefreshProfile)

    @Query("UPDATE refresh_profiles SET isSelected = 0 WHERE id != :selectedId")
    suspend fun clearSelectedExcept(selectedId: Int)

    // App profiles
    @Query("SELECT * FROM app_refresh_profiles ORDER BY appName ASC")
    fun getAllAppProfiles(): Flow<List<AppRefreshProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppProfile(appLog: AppRefreshProfile)

    @Delete
    suspend fun deleteAppProfile(appLog: AppRefreshProfile)

    // Optimization logging
    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC LIMIT 100")
    fun getLatestLogs(): Flow<List<OptimizationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: OptimizationLog)

    @Query("DELETE FROM optimization_logs")
    suspend fun clearLogs()
}
