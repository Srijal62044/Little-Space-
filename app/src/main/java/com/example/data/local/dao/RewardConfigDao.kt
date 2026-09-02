package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RewardConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardConfigDao {
    @Query("SELECT * FROM reward_config WHERE id = 1 LIMIT 1")
    fun getRewardConfig(): Flow<RewardConfigEntity?>

    @Query("SELECT * FROM reward_config WHERE id = 1 LIMIT 1")
    suspend fun getRewardConfigSync(): RewardConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: RewardConfigEntity)
}
