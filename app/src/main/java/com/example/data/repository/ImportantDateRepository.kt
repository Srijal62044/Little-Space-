package com.example.data.repository

import com.example.data.local.dao.ImportantDateDao
import com.example.data.local.entity.ImportantDateEntity
import kotlinx.coroutines.flow.Flow

class ImportantDateRepository(private val dateDao: ImportantDateDao) {
    val allDates: Flow<List<ImportantDateEntity>> = dateDao.getAllDates()

    suspend fun insertDate(date: ImportantDateEntity): Long = dateDao.insertDate(date)

    suspend fun updateDate(date: ImportantDateEntity) = dateDao.updateDate(date)

    suspend fun deleteDate(date: ImportantDateEntity) = dateDao.deleteDate(date)
}
