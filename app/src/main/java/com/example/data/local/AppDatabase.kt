package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        MoodEntryEntity::class,
        ImportantDateEntity::class,
        UserProfileEntity::class,
        ChatMessageEntity::class,
        RewardConfigEntity::class,
        DailyActivityEntity::class,
        BirthdayMemoryEntity::class,
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        RemixPresetEntity::class,
        GalleryCreationEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun moodDao(): MoodDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatDao(): ChatDao
    abstract fun rewardConfigDao(): RewardConfigDao
    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun birthdayMemoryDao(): BirthdayMemoryDao
    abstract fun musicDao(): MusicDao
    abstract fun galleryDao(): GalleryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "priyanka_little_space.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
