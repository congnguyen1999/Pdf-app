package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AiPreferencesManager
import com.example.data.model.AiProvider
import com.example.data.model.AiProviderConfig
import com.example.data.model.FlashcardEntity
import com.example.ui.theme.AccentAiBlue
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.OnAccentAiBlue
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryAmber

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, CardBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FlipFlashcardView(
    flashcard: FlashcardEntity,
    modifier: Modifier = Modifier,
    onFlip: () -> Unit = {}
) {
    var isFlipped by remember(flashcard.id) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flashcard_flip"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable {
                isFlipped = !isFlipped
                onFlip()
            }
            .testTag("flashcard_item_${flashcard.id}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (flashcard.isMastered) Color(0xFF2E7D32) else CardBorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(22.dp)) {
            if (rotation <= 90f) {
                // Front Side (Question)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = flashcard.keyConcept.ifBlank { "Câu hỏi" }.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 10.sp
                                ),
                                color = PrimaryPurple
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (flashcard.difficulty) {
                                "Dễ" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                                "Khó" -> Color(0xFFBA1A1A).copy(alpha = 0.12f)
                                else -> SecondaryAmber.copy(alpha = 0.12f)
                            }
                        ) {
                            Text(
                                text = "MỨC ĐỘ: ${flashcard.difficulty.uppercase()}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = when (flashcard.difficulty) {
                                    "Dễ" -> Color(0xFF2E7D32)
                                    "Khó" -> Color(0xFFBA1A1A)
                                    else -> SecondaryAmber
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = flashcard.front,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                lineHeight = 25.sp
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Chạm để lật",
                            modifier = Modifier.size(16.dp),
                            tint = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chạm vào thẻ để xem câu trả lời",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Back Side (Answer - flipped)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "💡 LỜI GIẢI / ĐÁP ÁN",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF059669)
                            )
                        }

                        if (flashcard.isMastered) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Đã thuộc",
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Đã thuộc",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = flashcard.back,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                fontSize = 15.sp
                            ),
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Chạm để lật lại",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chạm để quay lại câu hỏi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: Int = 14
) {
    val annotated = remember(text, textColor) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                var cursor = 0
                val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
                val matches = boldRegex.findAll(line).toList()

                if (matches.isEmpty()) {
                    append(line)
                } else {
                    for (match in matches) {
                        if (match.range.first > cursor) {
                            append(line.substring(cursor, match.range.first))
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                            append(match.groupValues[1])
                        }
                        cursor = match.range.last + 1
                    }
                    if (cursor < line.length) {
                        append(line.substring(cursor))
                    }
                }
                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.5).sp
        ),
        color = textColor
    )
}

@Composable
fun AiProviderSettingsDialog(
    currentConfig: AiProviderConfig,
    onDismiss: () -> Unit,
    onSave: (AiProviderConfig) -> Unit,
    onTestConnection: ((AiProviderConfig, (Boolean, String) -> Unit) -> Unit)? = null
) {
    var selectedProvider by remember { mutableStateOf(currentConfig.provider) }
    var apiKey by remember(selectedProvider) {
        mutableStateOf(if (selectedProvider == currentConfig.provider) currentConfig.apiKey else "")
    }
    var selectedModel by remember(selectedProvider) {
        mutableStateOf(if (selectedProvider == currentConfig.provider) currentConfig.model else selectedProvider.defaultModel)
    }
    var customEndpoint by remember(selectedProvider) {
        mutableStateOf(if (selectedProvider == currentConfig.provider) currentConfig.customEndpoint else selectedProvider.defaultEndpoint)
    }
    var showApiKey by remember { mutableStateOf(false) }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val activeConfigToTest = AiProviderConfig(
        provider = selectedProvider,
        apiKey = apiKey.trim(),
        model = selectedModel.trim().ifBlank { selectedProvider.defaultModel },
        customEndpoint = customEndpoint.trim().ifBlank { selectedProvider.defaultEndpoint }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Cài Đặt Nhà Cung Cấp AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Hỗ trợ Gemini, OpenAI, Claude, DeepSeek...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Provider Selection Chips
                Text(
                    text = "CHỌN NỀN TẢNG AI:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiProvider.entries.forEach { provider ->
                        val isSelected = provider == selectedProvider
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedProvider = provider
                                testResult = null
                            },
                            label = {
                                Text(
                                    text = provider.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (provider) {
                                        AiProvider.VILAO -> Icons.Default.AutoAwesome
                                        AiProvider.GEMINI -> Icons.Default.SmartToy
                                        AiProvider.OPENAI -> Icons.Default.Psychology
                                        AiProvider.DEEPSEEK -> Icons.Default.Speed
                                        AiProvider.CLAUDE -> Icons.Default.SmartToy
                                        AiProvider.GROQ -> Icons.Default.Speed
                                        AiProvider.CUSTOM -> Icons.Default.Dns
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Provider description banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when (selectedProvider) {
                                AiProvider.VILAO -> "Vilao AI: Cổng API AI chất lượng cao (Base URL mặc định: https://api.vilao.ai/v1)."
                                AiProvider.GEMINI -> "Google Gemini: Tốc độ cao, tối ưu phân tích sách tiếng Việt và tạo flashcards."
                                AiProvider.OPENAI -> "OpenAI: Hỗ trợ GPT-4o, GPT-4o-mini với độ chính xác cao."
                                AiProvider.DEEPSEEK -> "DeepSeek: Khả năng lập luận và giải thích sách chuyên sâu với chi phí tối ưu."
                                AiProvider.CLAUDE -> "Anthropic Claude: Văn phong tự nhiên, tóm tắt và phân tích học thuật sâu sắc."
                                AiProvider.GROQ -> "Groq: Tốc độ xử lý siêu nhanh (Llama 3.3 70B, Mixtral)."
                                AiProvider.CUSTOM -> "Tự do kết nối với Vilao AI (https://api.vilao.ai/v1), Ollama, vLLM hoặc máy chủ OpenAI-compatible."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // API Key Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "API KEY:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            testResult = null
                        },
                        label = { Text("Khóa API (${selectedProvider.displayName})") },
                        placeholder = {
                            Text(
                                when (selectedProvider) {
                                    AiProvider.VILAO -> "Nhập Vilao API Key..."
                                    AiProvider.GEMINI -> "AIzaSy... (Để trống dùng key mặc định)"
                                    AiProvider.OPENAI -> "sk-proj-..."
                                    AiProvider.DEEPSEEK -> "sk-..."
                                    AiProvider.CLAUDE -> "sk-ant-..."
                                    AiProvider.GROQ -> "gsk_..."
                                    AiProvider.CUSTOM -> "API Key (nếu có)"
                                }
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Ẩn API Key" else "Hiện API Key"
                                )
                            }
                        },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("api_key_input_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Model Selector & Quick Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "MÔ HÌNH (MODEL):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Quick Suggested Model Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedProvider.presetModels.forEach { modelName ->
                            val isModelSelected = selectedModel.trim().equals(modelName, ignoreCase = true)
                            val containerColor = if (isModelSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (isModelSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = containerColor,
                                modifier = Modifier.clickable {
                                    selectedModel = modelName
                                    testResult = null
                                }
                            ) {
                                Text(
                                    text = modelName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isModelSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = textColor
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = {
                            selectedModel = it
                            testResult = null
                        },
                        label = { Text("Tên Model") },
                        placeholder = { Text(selectedProvider.defaultModel) },
                        modifier = Modifier.fillMaxWidth().testTag("model_name_input_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Custom Endpoint (Show for Vilao, Custom, OpenAI, DeepSeek, Groq)
                if (selectedProvider == AiProvider.VILAO || selectedProvider == AiProvider.CUSTOM || selectedProvider == AiProvider.OPENAI || selectedProvider == AiProvider.DEEPSEEK || selectedProvider == AiProvider.GROQ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "BASE URL (API ENDPOINT):",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        OutlinedTextField(
                            value = customEndpoint,
                            onValueChange = {
                                customEndpoint = it
                                testResult = null
                            },
                            label = { Text("API Endpoint URL") },
                            placeholder = { Text(selectedProvider.defaultEndpoint) },
                            modifier = Modifier.fillMaxWidth().testTag("api_endpoint_input_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Test Connection Button & Result
                if (onTestConnection != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isTesting = true
                                testResult = null
                                onTestConnection(activeConfigToTest) { success, msg ->
                                    isTesting = false
                                    testResult = Pair(success, msg)
                                }
                            },
                            enabled = !isTesting,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_ai_connection_btn")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang kiểm tra...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kiểm tra kết nối", fontSize = 12.sp)
                            }
                        }

                        if (testResult != null) {
                            val (success, msg) = testResult!!
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (success) Color(0xFF2E7D32).copy(alpha = 0.12f) else Color(0xFFBA1A1A).copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (success) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (success) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (success) "Kết nối tốt" else "Thất bại",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (success) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    testResult?.let { (success, msg) ->
                        if (!success) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(activeConfigToTest)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_api_key_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lưu Cấu Hình")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
fun ApiKeyConfigDialog(
    currentKey: String,
    selectedModel: String,
    onDismiss: () -> Unit,
    onSave: (apiKey: String, model: String) -> Unit
) {
    AiProviderSettingsDialog(
        currentConfig = AiProviderConfig(
            provider = AiProvider.GEMINI,
            apiKey = currentKey,
            model = selectedModel
        ),
        onDismiss = onDismiss,
        onSave = { config ->
            onSave(config.apiKey, config.model)
        }
    )
}

