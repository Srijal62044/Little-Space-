package com.example.data.repository

import com.example.data.local.dao.MoodDao
import com.example.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

class MoodRepository(private val moodDao: MoodDao) {
    val allMoodEntries: Flow<List<MoodEntryEntity>> = moodDao.getAllMoodEntries()

    fun getTodayMood(dateString: String): Flow<MoodEntryEntity?> = moodDao.getTodayMood(dateString)

    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long = moodDao.insertMoodEntry(entry)

    suspend fun deleteMoodEntry(entry: MoodEntryEntity) = moodDao.deleteMoodEntry(entry)
}
