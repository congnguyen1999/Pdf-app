package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessageEntity
import com.example.data.model.StudyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE bookId = :bookId ORDER BY timestamp ASC")
    fun getChatMessages(bookId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE bookId = :bookId")
    suspend fun clearChatHistory(bookId: Long)
}

@Dao
interface StudyPlanDao {
    @Query("SELECT * FROM study_plans WHERE bookId = :bookId ORDER BY dayNumber ASC")
    fun getStudyPlans(bookId: Long): Flow<List<StudyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlans(plans: List<StudyPlanEntity>)

    @Query("UPDATE study_plans SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updatePlanCompletion(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM study_plans WHERE bookId = :bookId")
    suspend fun deleteStudyPlans(bookId: Long)
}
