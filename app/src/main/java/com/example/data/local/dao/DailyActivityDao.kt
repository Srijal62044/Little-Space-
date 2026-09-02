package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {
    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    suspend fun getActivityForDate(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun getAllActivities(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    suspend fun getAllActivitiesSync(): List<DailyActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordActivity(activity: DailyActivityEntity)

    @Query("SELECT COUNT(*) FROM daily_activity WHERE isStreakAchieved = 1")
    fun getTotalCompletedDaysFlow(): Flow<Int>

    @Query("DELETE FROM daily_activity")
    suspend fun deleteAllActivities()
}
