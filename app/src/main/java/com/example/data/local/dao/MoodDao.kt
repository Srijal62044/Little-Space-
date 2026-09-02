package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries WHERE dateString = :dateString ORDER BY timestamp DESC LIMIT 1")
    fun getTodayMood(dateString: String): Flow<MoodEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long

    @Delete
    suspend fun deleteMoodEntry(entry: MoodEntryEntity)
}
