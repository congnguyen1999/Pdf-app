package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FlashcardEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.StudyPlanEntity
import com.example.ui.components.AiProviderSettingsDialog
import com.example.ui.components.ApiKeyConfigDialog
import com.example.ui.components.FlipFlashcardView
import com.example.ui.components.FormattedMarkdownText
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentAiBlue
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.OnAccentAiBlue
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.BookStudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookStudyViewModel,
    book: BookEntity,
    chapters: List<ChapterEntity>,
    flashcards: List<FlashcardEntity>,
    quizQuestions: List<QuizQuestionEntity>,
    chatMessages: List<ChatMessageEntity>,
    studyPlans: List<StudyPlanEntity>,
    activeTab: Int,
    currentPdfPage: Int,
    pdfPageBitmap: Bitmap?,
    isRenderingPdf: Boolean,
    isAiLoading: Boolean,
    aiOperationText: String,
    aiError: String?,
    pageExplanation: String?,
    isExplainingPage: Boolean,
    isTtsPlaying: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val activeAiConfig by viewModel.activeAiConfig.collectAsStateWithLifecycle()

    val tabs = listOf(
        Triple("Tổng Quan", Icons.Default.MenuBook, 0),
        Triple("Đọc PDF", Icons.Default.PictureAsPdf, 1),
        Triple("Flashcard", Icons.Default.Flip, 2),
        Triple("Trắc Nghiệm", Icons.Default.Quiz, 3),
        Triple("Gia Sư AI", Icons.AutoMirrored.Filled.Chat, 4),
        Triple("Lộ Trình", Icons.Default.DateRange, 5)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${book.author} • ${book.totalPages} trang",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("book_detail_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.speakSummaryText(book.summary) },
                        modifier = Modifier.testTag("tts_summary_btn")
                    ) {
                        Icon(
                            imageVector = if (isTtsPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "Nghe đọc tóm tắt",
                            tint = if (isTtsPlaying) SecondaryAmber else PrimaryPurple
                        )
                    }
                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "API Key", tint = PrimaryPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryPurple
            ) {
                tabs.forEach { (title, icon, index) ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { viewModel.setActiveTab(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        modifier = Modifier.testTag("study_tab_$index")
                    )
                }
            }

            // Error Notice Banner (if any)
            if (aiError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = aiError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(onClick = { viewModel.clearAiError() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Active Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (activeTab) {
                    0 -> SummaryTab(book, chapters, viewModel)
                    1 -> PdfReaderTab(book, currentPdfPage, pdfPageBitmap, isRenderingPdf, isExplainingPage, viewModel)
                    2 -> FlashcardsTab(book, flashcards, viewModel)
                    3 -> QuizTab(book, quizQuestions, viewModel)
                    4 -> AiTutorTab(book, chatMessages, viewModel)
                    5 -> StudyPlanTab(book, studyPlans, viewModel)
                }

                // Global AI Loading Overlay
                if (isAiLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryPurple,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${activeAiConfig.provider.displayName} AI Đang Xử Lý",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = aiOperationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Page Explanation Dialog
    if (pageExplanation != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPageExplanation() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SecondaryAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Giải Thích Trang ${currentPdfPage + 1}", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    FormattedMarkdownText(text = pageExplanation)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissPageExplanation() }) {
                    Text("Đã Hiểu")
                }
            }
        )
    }

    // AI Provider Configuration Dialog
    if (showApiKeyDialog) {
        AiProviderSettingsDialog(
            currentConfig = activeAiConfig,
            onDismiss = { showApiKeyDialog = false },
            onSave = { newConfig ->
                viewModel.updateAiConfig(newConfig)
            },
            onTestConnection = { config, callback ->
                viewModel.testAiConnection(config, callback)
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 1: SUMMARY & CHAPTER BREAKDOWN
// -------------------------------------------------------------
@Composable
fun SummaryTab(
    book: BookEntity,
    chapters: List<ChapterEntity>,
    viewModel: BookStudyViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Re-generate or Analyze Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryPurple.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Phân Tích AI Gemini",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tự động trích xuất cấu trúc chương, tóm tắt và bài học cốt lõi từ sách.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { viewModel.triggerAiSummaryGeneration() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("ai_analyze_summary_btn")
                    ) {
                        Text("Phân Tích AI", fontSize = 12.sp)
                    }
                }
            }
        }

        // Overview Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TỔNG QUAN CUỐN SÁCH",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = PrimaryPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FormattedMarkdownText(
                        text = book.summary.ifBlank { "Chưa có tóm tắt. Hãy nhấn nút 'Phân Tích AI' phía trên để Gemini tổng hợp nội dung cuốn sách này." }
                    )
                }
            }
        }

        // Key Takeaways
        if (book.keyTakeaways.isNotBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = SecondaryAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BÀI HỌC VÀNG CỐT LÕI",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                                color = SecondaryAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormattedMarkdownText(text = book.keyTakeaways)
                    }
                }
            }
        }

        // Chapter Breakdown Header
        item {
            Text(
                text = "CÁC CHƯƠNG & Ý CHÍNH (${chapters.size} chương)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Chapters List
        if (chapters.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có danh sách chương chi tiết. Nhấn 'Phân Tích AI' để tự động bóc tách từng chương!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(chapters) { chapter ->
                ChapterAccordionItem(chapter)
            }
        }
    }
}

@Composable
fun ChapterAccordionItem(chapter: ChapterEntity) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chapter.chapterNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Thu gọn" else "Mở rộng",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = chapter.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (chapter.keyTakeaways.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Điểm cốt lõi:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FormattedMarkdownText(text = chapter.keyTakeaways, fontSize = 13)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: PDF PAGE READER & AI PAGE TUTOR
// -------------------------------------------------------------
@Composable
fun PdfReaderTab(
    book: BookEntity,
    currentPage: Int,
    pdfBitmap: Bitmap?,
    isRendering: Boolean,
    isExplaining: Boolean,
    viewModel: BookStudyViewModel
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewMode by remember { mutableIntStateOf(0) } // 0: Visual PDF, 1: Extracted Text
    val currentPageText by viewModel.currentPageText.collectAsStateWithLifecycle()

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3.5f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // View Mode Selector & Page Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryPurple.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đã quét đủ ${book.totalPages} trang",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryPurple
                        )
                    }
                }

                // Switch between Visual PDF and Extracted Text
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (viewMode == 0) PrimaryPurple else Color.Transparent,
                        modifier = Modifier.clickable { viewMode = 0 }
                    ) {
                        Text(
                            text = "Bản In PDF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (viewMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (viewMode == 1) PrimaryPurple else Color.Transparent,
                        modifier = Modifier.clickable { viewMode = 1 }
                    ) {
                        Text(
                            text = "Văn Bản AI",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (viewMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // PDF Page Canvas Viewer or Extracted Text Viewer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewMode == 0) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                if (viewMode == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .transformable(state = transformState),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRendering) {
                            CircularProgressIndicator(color = Color.White)
                        } else if (pdfBitmap != null) {
                            Image(
                                bitmap = pdfBitmap.asImageBitmap(),
                                contentDescription = "Trang ${currentPage + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Đang tải trang ${currentPage + 1}...",
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    // Extracted Text Mode
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nội dung trích xuất - Trang ${currentPage + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryPurple
                            )
                            IconButton(
                                onClick = { viewModel.speakSummaryText(currentPageText ?: "") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Đọc trang này",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        if (!currentPageText.isNullOrBlank()) {
                            Text(
                                text = currentPageText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Trang này chứa hình ảnh hoặc biểu đồ. AI đã ghi nhận cấu trúc trang đầy đủ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.previousPage() },
                        enabled = currentPage > 0,
                        modifier = Modifier.testTag("pdf_prev_page_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trang trước")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Trang ${currentPage + 1} / ${book.totalPages.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (book.totalPages > 1) {
                            Slider(
                                value = currentPage.toFloat(),
                                onValueChange = { viewModel.setPdfPage(it.toInt()) },
                                valueRange = 0f..(book.totalPages - 1).toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .testTag("pdf_page_slider")
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nextPage() },
                        enabled = currentPage < book.totalPages - 1,
                        modifier = Modifier.testTag("pdf_next_page_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Trang tiếp")
                    }
                }
            }
        }

        // Floating AI Explain Page button
        FloatingActionButton(
            onClick = { viewModel.explainCurrentPage() },
            containerColor = SecondaryAmber,
            contentColor = Color(0xFF0F172A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("ai_explain_page_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isExplaining) {
                    CircularProgressIndicator(color = Color(0xFF0F172A), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Giải thích trang")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Giải Thích Trang", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: FLASHCARDS (3D FLIP CARDS & MASTERY TRACKING)
// -------------------------------------------------------------
@Composable
fun FlashcardsTab(
    book: BookEntity,
    flashcards: List<FlashcardEntity>,
    viewModel: BookStudyViewModel
) {
    var currentIndex by remember(flashcards) { mutableIntStateOf(0) }

    if (flashcards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Chưa có Thẻ Flashcard nào",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hãy để Gemini AI phân tích sách và tự động tạo bộ thẻ ghi nhớ thông minh cho bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.triggerAiFlashcardGeneration(count = 6) },
                        modifier = Modifier.testTag("create_first_flashcards_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tạo 6 Thẻ Bằng Gemini AI")
                    }
                }
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, flashcards.size - 1)
    val currentCard = flashcards[safeIndex]
    val masteredCount = flashcards.count { it.isMastered }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header & Progress
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thẻ ${safeIndex + 1} / ${flashcards.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TertiaryEmerald.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Đã thuộc: $masteredCount/${flashcards.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TertiaryEmerald,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (safeIndex + 1).toFloat() / flashcards.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryPurple,
                trackColor = PrimaryPurple.copy(alpha = 0.15f)
            )
        }

        // 3D Flip Card View
        FlipFlashcardView(
            flashcard = currentCard,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Actions: Review vs Mastered
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentCard.isMastered) viewModel.toggleFlashcardMastery(currentCard)
                        if (safeIndex < flashcards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("flashcard_review_btn")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cần Ôn Lại", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (!currentCard.isMastered) viewModel.toggleFlashcardMastery(currentCard)
                        if (safeIndex < flashcards.size - 1) currentIndex++ else currentIndex = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TertiaryEmerald),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("flashcard_mastered_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đã Thuộc", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Controls & AI Generator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (safeIndex > 0) currentIndex-- },
                    enabled = safeIndex > 0
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Thẻ trước")
                }

                TextButton(
                    onClick = { viewModel.triggerAiFlashcardGeneration(count = 6) },
                    modifier = Modifier.testTag("ai_add_more_flashcards_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tạo thêm thẻ bằng AI", fontSize = 13.sp)
                }

                IconButton(
                    onClick = { if (safeIndex < flashcards.size - 1) currentIndex++ },
                    enabled = safeIndex < flashcards.size - 1
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Thẻ sau")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: QUIZ & PRACTICE TESTS
// -------------------------------------------------------------
@Composable
fun QuizTab(
    book: BookEntity,
    quizQuestions: List<QuizQuestionEntity>,
    viewModel: BookStudyViewModel
) {
    if (quizQuestions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = SecondaryAmber,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Chưa có Đề Thi Trắc Nghiệm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hãy để Gemini AI tạo bộ câu hỏi kiểm tra khả năng hiểu sâu và phản biện từ cuốn sách.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.triggerAiQuizGeneration(count = 5) },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryAmber, contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.testTag("create_first_quiz_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tạo Đề Thi 5 Câu Bằng AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val answeredCount = quizQuestions.count { it.selectedOptionIndex != null }
    val correctCount = quizQuestions.count { it.isAnsweredCorrectly == true }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Score Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (answeredCount == quizQuestions.size) TertiaryEmerald.copy(alpha = 0.12f) else PrimaryPurple.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KẾT QUẢ KIỂM TRA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đúng: $correctCount / $answeredCount / Tổng ${quizQuestions.size} câu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row {
                        IconButton(onClick = { viewModel.resetQuiz() }, modifier = Modifier.testTag("reset_quiz_btn")) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Làm lại bài thi")
                        }
                        IconButton(onClick = { viewModel.triggerAiQuizGeneration(count = 5) }, modifier = Modifier.testTag("ai_new_quiz_btn")) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Tạo đề mới", tint = PrimaryPurple)
                        }
                    }
                }
            }
        }

        // Quiz Questions List
        itemsIndexed(quizQuestions) { index, question ->
            QuizQuestionCard(
                questionIndex = index + 1,
                question = question,
                onSelectOption = { optIndex ->
                    viewModel.answerQuiz(question, optIndex)
                }
            )
        }
    }
}

@Composable
fun QuizQuestionCard(
    questionIndex: Int,
    question: QuizQuestionEntity,
    onSelectOption: (Int) -> Unit
) {
    val options = listOf(question.optionA, question.optionB, question.optionC, question.optionD).filter { it.isNotBlank() }
    val isAnswered = question.selectedOptionIndex != null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryPurple.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Câu $questionIndex",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryPurple
                    )
                }

                if (question.referenceChapter.isNotBlank()) {
                    Text(
                        text = question.referenceChapter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Options
            options.forEachIndexed { optIndex, optionText ->
                val isSelected = question.selectedOptionIndex == optIndex
                val isCorrect = question.correctOptionIndex == optIndex

                val (btnColor, textColor, borderColor) = when {
                    !isAnswered -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.outlineVariant)
                    isSelected && isCorrect -> Triple(TertiaryEmerald.copy(alpha = 0.15f), Color(0xFF059669), TertiaryEmerald)
                    isSelected && !isCorrect -> Triple(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFDC2626), Color(0xFFEF4444))
                    isCorrect -> Triple(TertiaryEmerald.copy(alpha = 0.15f), Color(0xFF059669), TertiaryEmerald)
                    else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = btnColor),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !isAnswered) { onSelectOption(optIndex) }
                        .testTag("quiz_opt_${questionIndex}_$optIndex")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prefix = when (optIndex) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            else -> "D"
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected || (isAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Explanation after answering
            if (isAnswered && question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = SecondaryAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Giải thích từ Gemini AI:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 5: AI TUTOR CHAT
// -------------------------------------------------------------
@Composable
fun AiTutorTab(
    book: BookEntity,
    chatMessages: List<ChatMessageEntity>,
    viewModel: BookStudyViewModel
) {
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "Tóm tắt ý quan trọng nhất trong 3 câu",
        "Cho 3 bài tập thực hành áp dụng vào đời sống",
        "Hãy đặt 1 câu hỏi kiểm tra độ hiểu của tôi",
        "Giải thích khái niệm khó nhất bằng ví dụ dễ hiểu"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                ChatBubbleItem(message = msg)
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AccentAiBlue,
                    border = BorderStroke(1.dp, Color(0xFFC2E7FF)),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = OnAccentAiBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Message Input Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Hỏi gia sư AI về cuốn sách...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_tutor_message_input"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = messageInput
                        messageInput = ""
                        viewModel.sendChatMessage(text)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple)
                        .testTag("send_chat_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gửi tin nhắn",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessageEntity) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentAiBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = OnAccentAiBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) PrimaryPurple else AccentAiBlue
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                FormattedMarkdownText(
                    text = message.content,
                    textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 6: STUDY PLAN & MILESTONES ROADMAP
// -------------------------------------------------------------
@Composable
fun StudyPlanTab(
    book: BookEntity,
    studyPlans: List<StudyPlanEntity>,
    viewModel: BookStudyViewModel
) {
    if (studyPlans.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TertiaryEmerald,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Chưa có Lộ Trình Học",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gemini AI sẽ phân bổ nội dung sách thành kế hoạch học 7 ngày rõ ràng, từng bước một.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.triggerAiStudyPlanGeneration(days = 7) },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryEmerald),
                        modifier = Modifier.testTag("create_first_study_plan_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thiết Kế Lộ Trình 7 Ngày", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val completedDays = studyPlans.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Roadmap Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryEmerald.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, TertiaryEmerald.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TIẾN ĐỘ LỘ TRÌNH",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TertiaryEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Đã hoàn thành $completedDays / ${studyPlans.size} ngày",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerAiStudyPlanGeneration(days = 7) },
                        colors = ButtonDefaults.buttonColors(containerColor = TertiaryEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("ai_recreate_study_plan_btn")
                    ) {
                        Text("Tạo Lại", fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Milestone Cards
        items(studyPlans) { plan ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (plan.isCompleted) TertiaryEmerald.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (plan.isCompleted) TertiaryEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = plan.isCompleted,
                        onCheckedChange = { viewModel.togglePlanItem(plan) },
                        modifier = Modifier.testTag("plan_checkbox_${plan.id}")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ngày ${plan.dayNumber}: ${plan.dayTitle}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (plan.isCompleted) TertiaryEmerald else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (plan.focusGoal.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mục tiêu: ${plan.focusGoal}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (plan.actionItems.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FormattedMarkdownText(text = plan.actionItems, fontSize = 13)
                        }
                    }
                }
            }
        }
    }
}
