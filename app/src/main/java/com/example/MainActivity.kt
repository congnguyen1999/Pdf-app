package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BookStudyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BookStudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PdfStudyApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PdfStudyApp(viewModel: BookStudyViewModel) {
    val books by viewModel.allBooks.collectAsState()
    val selectedBookId by viewModel.selectedBookId.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    val quizQuestions by viewModel.quizQuestions.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val studyPlans by viewModel.studyPlans.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val currentPdfPage by viewModel.currentPdfPage.collectAsState()
    val pdfPageBitmap by viewModel.pdfPageBitmap.collectAsState()
    val isRenderingPdf by viewModel.isRenderingPdf.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiOperationText by viewModel.aiOperationText.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val pageExplanation by viewModel.pageExplanation.collectAsState()
    val isExplainingPage by viewModel.isExplainingPage.collectAsState()
    val isTtsPlaying by viewModel.isTtsPlaying.collectAsState()

    if (selectedBook != null) {
        BackHandler {
            viewModel.selectBook(null)
        }

        BookDetailScreen(
            viewModel = viewModel,
            book = selectedBook!!,
            chapters = chapters,
            flashcards = flashcards,
            quizQuestions = quizQuestions,
            chatMessages = chatMessages,
            studyPlans = studyPlans,
            activeTab = activeTab,
            currentPdfPage = currentPdfPage,
            pdfPageBitmap = pdfPageBitmap,
            isRenderingPdf = isRenderingPdf,
            isAiLoading = isAiLoading,
            aiOperationText = aiOperationText,
            aiError = aiError,
            pageExplanation = pageExplanation,
            isExplainingPage = isExplainingPage,
            isTtsPlaying = isTtsPlaying,
            onBack = { viewModel.selectBook(null) }
        )
    } else {
        LibraryScreen(
            viewModel = viewModel,
            books = books,
            onBookSelected = { bookId ->
                viewModel.selectBook(bookId)
            }
        )
    }
}
