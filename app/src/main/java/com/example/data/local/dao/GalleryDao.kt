package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.GalleryCreationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    @Query("SELECT * FROM gallery_creations ORDER BY createdAt DESC")
    fun getAllCreations(): Flow<List<GalleryCreationEntity>>

    @Query("SELECT * FROM gallery_creations WHERE id = :id")
    suspend fun getCreationById(id: Long): GalleryCreationEntity?

    @Query("SELECT * FROM gallery_creations WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteCreations(): Flow<List<GalleryCreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: GalleryCreationEntity): Long

    @Update
    suspend fun updateCreation(creation: GalleryCreationEntity)

    @Delete
    suspend fun deleteCreation(creation: GalleryCreationEntity)

    @Query("DELETE FROM gallery_creations WHERE id = :id")
    suspend fun deleteCreationById(id: Long)

    @Query("UPDATE gallery_creations SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)
}
