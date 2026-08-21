package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntity
import com.example.ui.components.AiProviderSettingsDialog
import com.example.ui.components.ApiKeyConfigDialog
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
fun LibraryScreen(
    viewModel: BookStudyViewModel,
    books: List<BookEntity>,
    onBookSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "user", "sample"
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    var pendingUploadName by remember { mutableStateOf("") }
    var customTitleInput by remember { mutableStateOf("") }

    val activeAiConfig by viewModel.activeAiConfig.collectAsStateWithLifecycle()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "book_${System.currentTimeMillis()}.pdf"
            pendingUploadUri = uri
            pendingUploadName = fileName
            customTitleInput = fileName.removeSuffix(".pdf").replace("_", " ").replace("-", " ")
        }
    }

    val filteredBooks = books.filter { book ->
        val matchSearch = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true)
        val matchFilter = when (selectedFilter) {
            "sample" -> book.isSample
            "user" -> !book.isSample
            else -> true
        }
        matchSearch && matchFilter
    }

    val featuredBook = books.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CHÀO BUỔI SÁNG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontSize = 10.5.sp
                            ),
                            color = PrimaryPurple
                        )
                        Text(
                            text = "Thư viện của bạn",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                            .testTag("open_api_key_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Cấu hình API Key",
                            tint = OnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { pdfPickerLauncher.launch("application/pdf") },
                containerColor = AccentAiBlue,
                contentColor = OnAccentAiBlue,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("upload_pdf_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Tải lên PDF",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tải Lên PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // 1. Featured Book Clean Minimalist Card (Atomic Habits or First Book)
            if (featuredBook != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .clickable { onBookSelected(featuredBook.id) }
                            .testTag("featured_book_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, CardBorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Book Cover Icon Card
                            Box(
                                modifier = Modifier
                                    .size(width = 84.dp, height = 112.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(featuredBook.coverColorHex))
                                        } catch (e: Exception) {
                                            PrimaryPurple
                                        }
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = if (featuredBook.isSample) "AI SAMPLE" else "PDF",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = featuredBook.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = featuredBook.author,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { (featuredBook.studyProgress.coerceAtLeast(5)) / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = PrimaryPurple,
                                    trackColor = CardBorderSubtle
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "ĐÃ ĐỌC ${featuredBook.studyProgress}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 10.sp
                                    ),
                                    color = PrimaryPurple
                                )
                            }
                        }
                    }
                }
            }

            // 2. Gemini AI Assistant Highlight Section (Clean Minimalism Sky Accent)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .testTag("ai_gemini_highlight_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentAiBlue
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = OnAccentAiBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trợ lý AI (${activeAiConfig.provider.displayName})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = OnAccentAiBlue
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.35f),
                                modifier = Modifier.clickable { showApiKeyDialog = true }
                            ) {
                                Text(
                                    text = activeAiConfig.model,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp
                                    ),
                                    color = OnAccentAiBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AI Message bubble
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(Color.White.copy(alpha = 0.7f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Chào bạn! Tôi có thể tóm tắt sâu từng chương, giải thích thuật ngữ, và tạo đề thi trắc nghiệm từ bất kỳ tài liệu PDF nào.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (books.isNotEmpty()) onBookSelected(books.first().id)
                                    }
                            ) {
                                Text(
                                    text = "Giải thích thuật ngữ",
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = OnAccentAiBlue,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (books.isNotEmpty()) onBookSelected(books.first().id)
                                    }
                            ) {
                                Text(
                                    text = "Kiểm tra kiến thức",
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = OnAccentAiBlue,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 3. Stats Overview
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val avgProgress = if (books.isNotEmpty()) books.map { it.studyProgress }.average().toInt() else 0
                    StatCard(
                        title = "Tổng Sách",
                        value = "${books.size}",
                        icon = Icons.Default.LocalLibrary,
                        color = PrimaryPurple,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tiến Độ Đọc",
                        value = "$avgProgress%",
                        icon = Icons.Default.School,
                        color = TertiaryEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Search and Filters
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_books_field"),
                        placeholder = {
                            Text(
                                "Tìm kiếm sách, tác giả...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = CardBorderLight,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" },
                            label = { Text("Tất cả (${books.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor = OnPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == "all",
                                borderColor = CardBorderLight
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "user",
                            onClick = { selectedFilter = "user" },
                            label = { Text("Đã tải lên (${books.count { !it.isSample }})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor = OnPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == "user",
                                borderColor = CardBorderLight
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == "sample",
                            onClick = { selectedFilter = "sample" },
                            label = { Text("Mẫu AI (${books.count { it.isSample }})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryContainer,
                                selectedLabelColor = OnPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == "sample",
                                borderColor = CardBorderLight
                            )
                        )
                    }
                }
            }

            // 5. Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TỦ SÁCH CỦA BẠN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = PrimaryPurple
                    )
                }
            }

            // 6. Book Items
            if (filteredBooks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Không tìm thấy cuốn sách nào",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hãy nhấn 'Tải Lên PDF' để thêm sách mới và bắt đầu học!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredBooks, key = { it.id }) { book ->
                    BookItemCard(
                        book = book,
                        onClick = { onBookSelected(book.id) },
                        onDelete = { viewModel.deleteBook(book.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Pending PDF Upload Dialog
    if (pendingUploadUri != null) {
        AlertDialog(
            onDismissRequest = { pendingUploadUri = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Xác Nhận Tải Lên Sách PDF", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    Text(
                        text = "Tệp: $pendingUploadName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customTitleInput,
                        onValueChange = { customTitleInput = it },
                        label = { Text("Tên sách / Tiêu đề học tập") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_book_title_field"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingUploadUri ?: return@Button
                        val fileName = pendingUploadName
                        val title = customTitleInput
                        pendingUploadUri = null
                        viewModel.importPdf(uri, fileName, title) { newId ->
                            onBookSelected(newId)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    modifier = Modifier.testTag("confirm_upload_pdf_btn")
                ) {
                    Text("Tải Lên & Vào Học")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUploadUri = null }) {
                    Text("Hủy")
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

@Composable
fun BookItemCard(
    book: BookEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("book_item_${book.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover Tile
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 86.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(book.coverColorHex))
                        } catch (e: Exception) {
                            PrimaryPurple
                        }
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (book.isSample) "AI SAMPLE" else "PDF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Book Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("book_menu_btn_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Tùy chọn",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Xóa sách này") },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Page count & Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${book.totalPages} trang",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${book.studyProgress}% hoàn thành",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { book.studyProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryPurple,
                    trackColor = CardBorderSubtle
                )
            }
        }
    }
}

