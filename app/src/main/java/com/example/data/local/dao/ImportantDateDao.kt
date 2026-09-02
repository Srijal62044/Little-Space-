package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ImportantDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportantDateDao {
    @Query("SELECT * FROM important_dates ORDER BY targetDate ASC")
    fun getAllDates(): Flow<List<ImportantDateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDate(date: ImportantDateEntity): Long

    @Update
    suspend fun updateDate(date: ImportantDateEntity)

    @Delete
    suspend fun deleteDate(date: ImportantDateEntity)
}
