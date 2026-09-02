package com.example.data.repository

import com.example.data.local.dao.BirthdayMemoryDao
import com.example.data.local.dao.DailyActivityDao
import com.example.data.local.dao.RewardConfigDao
import com.example.data.local.entity.BirthdayMemoryEntity
import com.example.data.local.entity.DailyActivityEntity
import com.example.data.local.entity.RewardConfigEntity
import kotlinx.coroutines.flow.Flow

class RewardRepository(
    private val rewardConfigDao: RewardConfigDao,
    private val dailyActivityDao: DailyActivityDao,
    private val birthdayMemoryDao: BirthdayMemoryDao
) {
    val rewardConfig: Flow<RewardConfigEntity?> = rewardConfigDao.getRewardConfig()
    val allActivities: Flow<List<DailyActivityEntity>> = dailyActivityDao.getAllActivities()
    val totalCompletedDays: Flow<Int> = dailyActivityDao.getTotalCompletedDaysFlow()
    val allMemories: Flow<List<BirthdayMemoryEntity>> = birthdayMemoryDao.getAllMemories()

    suspend fun getRewardConfigOnce(): RewardConfigEntity? = rewardConfigDao.getRewardConfigSync()

    suspend fun saveRewardConfig(config: RewardConfigEntity) {
        rewardConfigDao.insertOrUpdate(config)
    }

    suspend fun recordDailyActivity(activity: DailyActivityEntity) {
        dailyActivityDao.recordActivity(activity)
    }

    suspend fun getActivityForDate(date: String): DailyActivityEntity? {
        return dailyActivityDao.getActivityForDate(date)
    }

    suspend fun getAllActivitiesOnce(): List<DailyActivityEntity> {
        return dailyActivityDao.getAllActivitiesSync()
    }

    suspend fun clearAllActivities() {
        dailyActivityDao.deleteAllActivities()
    }

    suspend fun insertMemory(memory: BirthdayMemoryEntity): Long {
        return birthdayMemoryDao.insertMemory(memory)
    }

    suspend fun updateMemory(memory: BirthdayMemoryEntity) {
        birthdayMemoryDao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: BirthdayMemoryEntity) {
        birthdayMemoryDao.deleteMemory(memory)
    }

    suspend fun insertAllMemories(memories: List<BirthdayMemoryEntity>) {
        birthdayMemoryDao.insertAll(memories)
    }
}
