package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiProvider
import com.example.data.model.AiProviderConfig
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.data.pdf.PdfHelper
import com.example.data.repository.BookStudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class BookStudyViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val repository = BookStudyRepository(application)
    val aiPreferencesManager = repository.aiPreferencesManager

    val allBooks: StateFlow<List<BookEntity>> = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    private val _chapters = MutableStateFlow<List<ChapterEntity>>(emptyList())
    val chapters: StateFlow<List<ChapterEntity>> = _chapters.asStateFlow()

    private val _flashcards = MutableStateFlow<List<FlashcardEntity>>(emptyList())
    val flashcards: StateFlow<List<FlashcardEntity>> = _flashcards.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestionEntity>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestionEntity>> = _quizQuestions.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _chatMessages.asStateFlow()

    private val _studyPlans = MutableStateFlow<List<StudyPlanEntity>>(emptyList())
    val studyPlans: StateFlow<List<StudyPlanEntity>> = _studyPlans.asStateFlow()

    // Study Tab index (0: Overview, 1: Reader, 2: Flashcards, 3: Quiz, 4: AI Tutor, 5: Study Plan)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // PDF Reader State
    private val _currentPdfPage = MutableStateFlow(0) // 0-indexed
    val currentPdfPage: StateFlow<Int> = _currentPdfPage.asStateFlow()

    private val _pdfPageBitmap = MutableStateFlow<Bitmap?>(null)
    val pdfPageBitmap: StateFlow<Bitmap?> = _pdfPageBitmap.asStateFlow()

    private val _isRenderingPdf = MutableStateFlow(false)
    val isRenderingPdf: StateFlow<Boolean> = _isRenderingPdf.asStateFlow()

    private val _currentPageText = MutableStateFlow<String?>(null)
    val currentPageText: StateFlow<String?> = _currentPageText.asStateFlow()

    // AI Operation States
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiOperationText = MutableStateFlow("")
    val aiOperationText: StateFlow<String> = _aiOperationText.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _pageExplanation = MutableStateFlow<String?>(null)
    val pageExplanation: StateFlow<String?> = _pageExplanation.asStateFlow()

    private val _isExplainingPage = MutableStateFlow(false)
    val isExplainingPage: StateFlow<Boolean> = _isExplainingPage.asStateFlow()

    // AI Configuration State (Supports Gemini, OpenAI, DeepSeek, Claude, Groq, Custom)
    private val _activeAiConfig = MutableStateFlow(aiPreferencesManager.getActiveConfig())
    val activeAiConfig: StateFlow<AiProviderConfig> = _activeAiConfig.asStateFlow()

    // Backward compatible getters
    val customApiKey: StateFlow<String> get() = MutableStateFlow(_activeAiConfig.value.apiKey).asStateFlow()
    val selectedAiModel: StateFlow<String> get() = MutableStateFlow(_activeAiConfig.value.model).asStateFlow()

    // TTS Audio Reader
    private var tts: TextToSpeech? = null
    private val _isTtsPlaying = MutableStateFlow(false)
    val isTtsPlaying: StateFlow<Boolean> = _isTtsPlaying.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDatabaseIfEmpty()
        }
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val vietnamese = Locale("vi", "VN")
            val res = tts?.setLanguage(vietnamese)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
        }
    }

    fun selectBook(bookId: Long?) {
        _selectedBookId.value = bookId
        if (bookId == null) {
            _selectedBook.value = null
            _currentPageText.value = null
            _currentPdfPage.value = 0
            _aiError.value = null
            stopTts()
            return
        }

        _currentPdfPage.value = 0
        _aiError.value = null

        viewModelScope.launch {
            repository.getBook(bookId).collect { book ->
                _selectedBook.value = book
                if (book != null) {
                    loadCurrentPageBitmap(book.filePath, _currentPdfPage.value)
                }
            }
        }

        viewModelScope.launch {
            val pageEntity = repository.getPage(bookId, 1)
            _currentPageText.value = pageEntity?.textContent
        }

        viewModelScope.launch {
            repository.getChapters(bookId).collect { _chapters.value = it }
        }
        viewModelScope.launch {
            repository.getFlashcards(bookId).collect { _flashcards.value = it }
        }
        viewModelScope.launch {
            repository.getQuizQuestions(bookId).collect { _quizQuestions.value = it }
        }
        viewModelScope.launch {
            repository.getChatMessages(bookId).collect { _chatMessages.value = it }
        }
        viewModelScope.launch {
            repository.getStudyPlans(bookId).collect { _studyPlans.value = it }
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun updateAiConfig(config: AiProviderConfig) {
        _activeAiConfig.value = config
        aiPreferencesManager.saveActiveConfig(config)
    }

    fun setCustomApiKey(key: String, model: String) {
        val current = _activeAiConfig.value
        val updated = current.copy(apiKey = key.trim(), model = model.trim())
        updateAiConfig(updated)
    }

    fun testAiConnection(config: AiProviderConfig, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.testConnection(config)
            result.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Lỗi không xác định khi kết nối API.")
            }
        }
    }

    fun clearAiError() {
        _aiError.value = null
    }

    // PDF Navigation
    fun setPdfPage(pageIndex: Int) {
        val book = _selectedBook.value ?: return
        val validPage = pageIndex.coerceIn(0, (book.totalPages - 1).coerceAtLeast(0))
        _currentPdfPage.value = validPage
        loadCurrentPageBitmap(book.filePath, validPage)
        
        viewModelScope.launch {
            val pageEntity = repository.getPage(book.id, validPage + 1)
            _currentPageText.value = pageEntity?.textContent
        }

        // Update progress
        val progress = if (book.totalPages > 0) ((validPage + 1) * 100 / book.totalPages).coerceIn(0, 100) else 0
        viewModelScope.launch {
            repository.updateProgress(book.id, progress)
        }
    }

    fun nextPage() {
        setPdfPage(_currentPdfPage.value + 1)
    }

    fun previousPage() {
        setPdfPage(_currentPdfPage.value - 1)
    }

    private fun loadCurrentPageBitmap(filePath: String, pageIndex: Int) {
        if (filePath.isBlank()) return
        val book = _selectedBook.value
        viewModelScope.launch {
            _isRenderingPdf.value = true
            val bitmap = PdfHelper.renderPdfPage(filePath, pageIndex, densityMultiplier = 1.8f)
            _pdfPageBitmap.value = bitmap
            _isRenderingPdf.value = false

            if (book != null) {
                val pageEntity = repository.getPage(book.id, pageIndex + 1)
                _currentPageText.value = pageEntity?.textContent
            }
        }
    }

    // Import PDF
    fun importPdf(uri: Uri, fileName: String, customTitle: String? = null, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "Đang tải lên và xử lý tệp PDF..."
            try {
                val newId = repository.importPdfBook(uri, fileName, customTitle)
                _isAiLoading.value = false
                selectBook(newId)
                onComplete(newId)
            } catch (e: Exception) {
                _isAiLoading.value = false
                _aiError.value = "Không thể đọc tệp PDF: ${e.message}"
            }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            if (_selectedBookId.value == bookId) {
                _selectedBookId.value = null
            }
            repository.deleteBook(bookId)
        }
    }

    // AI Actions
    fun triggerAiSummaryGeneration() {
        val book = _selectedBook.value ?: return
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "${config.provider.displayName} (${config.model}) đang tóm tắt và phân tích các chương sách..."
            _aiError.value = null
            val result = repository.generateAiSummaryAndChapters(book.id, config)
            _isAiLoading.value = false
            result.onFailure { _aiError.value = it.message }
        }
    }

    fun triggerAiFlashcardGeneration(count: Int = 6) {
        val book = _selectedBook.value ?: return
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "${config.provider.displayName} (${config.model}) đang trích xuất khái niệm và tạo Flashcard..."
            _aiError.value = null
            val result = repository.generateAiFlashcards(book.id, count, config)
            _isAiLoading.value = false
            result.onFailure { _aiError.value = it.message }
        }
    }

    fun triggerAiQuizGeneration(count: Int = 5) {
        val book = _selectedBook.value ?: return
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "${config.provider.displayName} (${config.model}) đang biên soạn bộ câu hỏi trắc nghiệm..."
            _aiError.value = null
            val result = repository.generateAiQuiz(book.id, count, config)
            _isAiLoading.value = false
            result.onFailure { _aiError.value = it.message }
        }
    }

    fun triggerAiStudyPlanGeneration(days: Int = 7) {
        val book = _selectedBook.value ?: return
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "${config.provider.displayName} (${config.model}) đang thiết kế lộ trình học $days ngày..."
            _aiError.value = null
            val result = repository.generateAiStudyPlan(book.id, days, config)
            _isAiLoading.value = false
            result.onFailure { _aiError.value = it.message }
        }
    }

    fun explainCurrentPage() {
        val book = _selectedBook.value ?: return
        val pageNum = _currentPdfPage.value + 1
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isExplainingPage.value = true
            _pageExplanation.value = null
            val result = repository.explainPage(book.id, pageNum, config)
            _isExplainingPage.value = false
            result.onSuccess {
                _pageExplanation.value = it
            }.onFailure {
                _pageExplanation.value = "Lỗi giải thích trang từ ${config.provider.displayName}: ${it.message}"
            }
        }
    }

    fun dismissPageExplanation() {
        _pageExplanation.value = null
    }

    // Flashcard Interactions
    fun toggleFlashcardMastery(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.toggleFlashcardMastered(card)
        }
    }

    // Quiz Interactions
    fun answerQuiz(question: QuizQuestionEntity, selectedIndex: Int) {
        val isCorrect = selectedIndex == question.correctOptionIndex
        viewModelScope.launch {
            repository.answerQuizQuestion(question.id, selectedIndex, isCorrect)
        }
    }

    fun resetQuiz() {
        val bookId = _selectedBookId.value ?: return
        viewModelScope.launch {
            repository.resetQuiz(bookId)
        }
    }

    // Study Plan Interactions
    fun togglePlanItem(plan: StudyPlanEntity) {
        viewModelScope.launch {
            repository.togglePlanCompletion(plan.id, plan.isCompleted)
        }
    }

    // AI Tutor Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val bookId = _selectedBookId.value ?: return
        val config = _activeAiConfig.value
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiOperationText.value = "Gia sư ${config.provider.displayName} (${config.model}) đang suy nghĩ..."
            repository.sendChatMessage(bookId, text.trim(), config)
            _isAiLoading.value = false
        }
    }

    // Text-To-Speech (TTS)
    fun speakSummaryText(text: String) {
        if (_isTtsPlaying.value) {
            stopTts()
            return
        }
        if (text.isBlank()) return

        val cleanText = text.replace("**", "").replace("#", "").replace("•", "")
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "TTS_SUMMARY_UTTERANCE")
        _isTtsPlaying.value = true
    }

    fun stopTts() {
        tts?.stop()
        _isTtsPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
