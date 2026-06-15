package com.example.data

import kotlinx.coroutines.flow.Flow

class SpeedRepository(private val speedDao: SpeedDao) {

    val allProfiles: Flow<List<RefreshProfile>> = speedDao.getAllProfiles()
    val activeProfile: Flow<RefreshProfile?> = speedDao.getActiveProfile()
    val allAppProfiles: Flow<List<AppRefreshProfile>> = speedDao.getAllAppProfiles()
    val latestLogs: Flow<List<OptimizationLog>> = speedDao.getLatestLogs()

    suspend fun getActiveProfileOnce(): RefreshProfile? {
        return speedDao.getActiveProfileOnce()
    }

    suspend fun selectProfile(profileId: Int, currentProfiles: List<RefreshProfile>) {
        speedDao.clearSelectedExcept(profileId)
        val target = currentProfiles.find { it.id == profileId }
        if (target != null) {
            speedDao.updateProfile(target.copy(isSelected = true))
        }
    }

    suspend fun insertProfile(profile: RefreshProfile): Long {
        return speedDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: RefreshProfile) {
        speedDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: RefreshProfile) {
        speedDao.deleteProfile(profile)
    }

    suspend fun insertAppProfile(appLog: AppRefreshProfile) {
        speedDao.insertAppProfile(appLog)
    }

    suspend fun deleteAppProfile(appLog: AppRefreshProfile) {
        speedDao.deleteAppProfile(appLog)
    }

    suspend fun insertLog(log: OptimizationLog) {
        speedDao.insertLog(log)
    }

    suspend fun clearLogs() {
        speedDao.clearLogs()
    }
}
