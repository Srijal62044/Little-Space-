package com.example.data.repository

import com.example.data.local.dao.HabitDao
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>> = habitDao.getLogsForDate(dateString)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun toggleHabitLog(habitId: Long, dateString: String, isCurrentlyCompleted: Boolean, habit: HabitEntity) {
        if (isCurrentlyCompleted) {
            habitDao.deleteHabitLog(habitId, dateString)
            val newStreak = maxOf(0, habit.currentStreak - 1)
            habitDao.updateHabit(habit.copy(currentStreak = newStreak))
        } else {
            habitDao.insertHabitLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    isCompleted = true,
                    completedCount = 1
                )
            )
            val newStreak = habit.currentStreak + 1
            val newBest = maxOf(habit.bestStreak, newStreak)
            habitDao.updateHabit(habit.copy(currentStreak = newStreak, bestStreak = newBest))
        }
    }
}
