package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions WHERE bookId = :bookId ORDER BY id ASC")
    fun getQuizQuestionsByBookId(bookId: Long): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE bookId = :bookId ORDER BY id ASC")
    suspend fun getQuizQuestionsByBookIdOnce(bookId: Long): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuizQuestion(question: QuizQuestionEntity)

    @Query("UPDATE quiz_questions SET selectedOptionIndex = :selectedIndex, isAnsweredCorrectly = :isCorrect WHERE id = :id")
    suspend fun recordAnswer(id: Long, selectedIndex: Int, isCorrect: Boolean)

    @Query("UPDATE quiz_questions SET selectedOptionIndex = NULL, isAnsweredCorrectly = NULL WHERE bookId = :bookId")
    suspend fun resetQuizAnswers(bookId: Long)

    @Query("DELETE FROM quiz_questions WHERE bookId = :bookId")
    suspend fun deleteQuizByBookId(bookId: Long)
}
