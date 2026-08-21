package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        FlashcardEntity::class,
        QuizQuestionEntity::class,
        ChatMessageEntity::class,
        StudyPlanEntity::class,
        com.example.data.model.BookPageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizDao(): QuizDao
    abstract fun chatDao(): ChatDao
    abstract fun studyPlanDao(): StudyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_study_ai_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
