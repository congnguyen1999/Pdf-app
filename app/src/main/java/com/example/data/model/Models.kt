package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "Tác giả chưa xác định",
    val totalPages: Int = 1,
    val fileSize: Long = 0L,
    val filePath: String = "",
    val isSample: Boolean = false,
    val coverColorHex: String = "#4F46E5",
    val summary: String = "",
    val coreThemes: String = "",
    val keyTakeaways: String = "",
    val studyProgress: Int = 0, // 0 - 100%
    val dateAdded: Long = System.currentTimeMillis(),
    val lastStudied: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterNumber: Int,
    val title: String,
    val startPage: Int = 1,
    val endPage: Int = 1,
    val summary: String = "",
    val keyTakeaways: String = ""
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val chapterTitle: String = "Tổng quát",
    val front: String,
    val back: String,
    val keyConcept: String = "",
    val difficulty: String = "Vừa", // Dễ, Vừa, Khó
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val lastReviewed: Long = 0L
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOptionIndex: Int = 0, // 0 to 3
    val explanation: String = "",
    val referenceChapter: String = "",
    val selectedOptionIndex: Int? = null,
    val isAnsweredCorrectly: Boolean? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val sender: String, // "user" or "gemini"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuggestedPrompt: Boolean = false
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val title: String,
    val dayNumber: Int,
    val dayTitle: String,
    val focusGoal: String,
    val actionItems: String, // Comma or newline separated
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "book_pages",
    indices = [androidx.room.Index(value = ["bookId", "pageNumber"], unique = true)]
)
data class BookPageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val pageNumber: Int, // 1-indexed (1, 2, 3...)
    val textContent: String,
    val wordCount: Int = 0
)

// Gemini API Moshi Models
@JsonClass(generateAdapter = true)
data class GeminiApiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inline_data: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mime_type: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiApiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// DTOs for parsing AI outputs
@JsonClass(generateAdapter = true)
data class GeneratedSummaryDto(
    val title: String = "",
    val author: String = "",
    val overview: String = "",
    val coreThemes: List<String> = emptyList(),
    val keyTakeaways: List<String> = emptyList(),
    val chapters: List<GeneratedChapterDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedChapterDto(
    val chapterNumber: Int = 1,
    val title: String = "",
    val summary: String = "",
    val keyPoints: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedFlashcardDto(
    val front: String = "",
    val back: String = "",
    val concept: String = "",
    val difficulty: String = "Vừa",
    val chapter: String = ""
)

@JsonClass(generateAdapter = true)
data class GeneratedQuizDto(
    val questions: List<GeneratedQuestionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedQuestionDto(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = "",
    val reference: String = ""
)

@JsonClass(generateAdapter = true)
data class GeneratedStudyPlanDto(
    val days: List<GeneratedStudyDayDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GeneratedStudyDayDto(
    val day: Int = 1,
    val title: String = "",
    val goal: String = "",
    val tasks: List<String> = emptyList()
)
