package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookEntity
import com.example.data.model.BookPageEntity
import com.example.data.model.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastStudied DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdOnce(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET studyProgress = :progress, lastStudied = :timestamp WHERE id = :bookId")
    suspend fun updateProgress(bookId: Long, progress: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    fun getChaptersByBookId(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterNumber ASC")
    suspend fun getChaptersByBookIdOnce(bookId: Long): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: Long)

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getPagesByBookId(bookId: Long): Flow<List<BookPageEntity>>

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId ORDER BY pageNumber ASC")
    suspend fun getPagesByBookIdOnce(bookId: Long): List<BookPageEntity>

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getPageByNumber(bookId: Long, pageNumber: Int): BookPageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<BookPageEntity>)

    @Query("DELETE FROM book_pages WHERE bookId = :bookId")
    suspend fun deletePagesByBookId(bookId: Long)
}
