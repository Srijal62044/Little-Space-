package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }
}
