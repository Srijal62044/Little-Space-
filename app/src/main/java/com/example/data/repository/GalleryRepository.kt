package com.example.data.repository

import com.example.data.local.dao.GalleryDao
import com.example.data.local.entity.GalleryCreationEntity
import kotlinx.coroutines.flow.Flow

class GalleryRepository(private val galleryDao: GalleryDao) {
    val allCreations: Flow<List<GalleryCreationEntity>> = galleryDao.getAllCreations()
    val favoriteCreations: Flow<List<GalleryCreationEntity>> = galleryDao.getFavoriteCreations()

    suspend fun getCreationById(id: Long): GalleryCreationEntity? = galleryDao.getCreationById(id)

    suspend fun saveCreation(creation: GalleryCreationEntity): Long = galleryDao.insertCreation(creation)

    suspend fun updateCreation(creation: GalleryCreationEntity) = galleryDao.updateCreation(creation)

    suspend fun deleteCreation(creation: GalleryCreationEntity) = galleryDao.deleteCreation(creation)

    suspend fun deleteCreationById(id: Long) = galleryDao.deleteCreationById(id)

    suspend fun toggleFavorite(id: Long, isFav: Boolean) = galleryDao.setFavorite(id, isFav)
}
