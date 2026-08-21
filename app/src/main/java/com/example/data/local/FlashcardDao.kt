package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE bookId = :bookId ORDER BY id ASC")
    fun getFlashcardsByBookId(bookId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE bookId = :bookId ORDER BY id ASC")
    suspend fun getFlashcardsByBookIdOnce(bookId: Long): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards WHERE bookId = :bookId")
    fun getTotalFlashcardsCount(bookId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM flashcards WHERE bookId = :bookId AND isMastered = 1")
    fun getMasteredFlashcardsCount(bookId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Query("UPDATE flashcards SET isMastered = :isMastered, reviewCount = reviewCount + 1, lastReviewed = :timestamp WHERE id = :id")
    suspend fun updateMasteryStatus(id: Long, isMastered: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcardById(id: Long)

    @Query("DELETE FROM flashcards WHERE bookId = :bookId")
    suspend fun deleteFlashcardsByBookId(bookId: Long)
}
