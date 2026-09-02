package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BirthdayMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayMemoryDao {
    @Query("SELECT * FROM birthday_memories ORDER BY orderIndex ASC, id ASC")
    fun getAllMemories(): Flow<List<BirthdayMemoryEntity>>

    @Query("SELECT * FROM birthday_memories ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllMemoriesSync(): List<BirthdayMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: BirthdayMemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: BirthdayMemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: BirthdayMemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<BirthdayMemoryEntity>)
}
