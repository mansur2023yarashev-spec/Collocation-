package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CollocationItem
import com.example.data.model.VerbGroup
import com.example.ui.components.GeminiDialog
import com.example.ui.components.SubtleFadeInListItem
import com.example.ui.theme.OxfordBg
import com.example.ui.theme.OxfordBorder
import com.example.ui.theme.OxfordButtonWhite
import com.example.ui.theme.OxfordCard
import com.example.ui.theme.OxfordHeartActive
import com.example.ui.theme.OxfordRowBorder
import com.example.ui.theme.OxfordSearchBarBg
import com.example.ui.theme.OxfordSelectedTab
import com.example.ui.theme.OxfordTextPrimary
import com.example.ui.theme.OxfordTextSecondary
import com.example.util.GeminiHelper
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.launch

@Composable
fun CollocationsScreen(
    verbGroup: VerbGroup,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    ttsHelper: TextToSpeechHelper?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showGeminiDialog by remember { mutableStateOf(false) }

    // Ephemeral state for active Gemini analysis (strictly NO cache, fresh on each request)
    val expandedAiKeys = remember { mutableStateMapOf<String, Boolean>() }
    val aiResults = remember { mutableStateMapOf<String, String>() }
    val aiLoading = remember { mutableStateMapOf<String, Boolean>() }
    val aiErrors = remember { mutableStateMapOf<String, String>() }
    val aiHistory = remember { mutableStateMapOf<String, List<String>>() }

    // Clear everything when exiting the screen to avoid holding cached results
    DisposableEffect(Unit) {
        onDispose {
            aiResults.clear()
            expandedAiKeys.clear()
            aiErrors.clear()
            aiLoading.clear()
            aiHistory.clear()
        }
    }

    fun generateFreshResult(itemKey: String, displayPhrase: String, item: CollocationItem) {
        val previousList = aiHistory[itemKey] ?: emptyList()
        // Clear previous result immediately so stale content is never shown
        aiResults.remove(itemKey)
        aiErrors.remove(itemKey)
        aiLoading[itemKey] = true

        scope.launch {
            val res = GeminiHelper.explainCollocation(
                context = context,
                category = "verb",
                verb = verbGroup.verb,
                collocation = displayPhrase,
                noun = item.noun,
                meaning = item.meaning,
                previousExamples = previousList
            )
            aiLoading[itemKey] = false
            res.fold(
                onSuccess = { explanation ->
                    aiResults[itemKey] = explanation
                    aiHistory[itemKey] = previousList + explanation
                },
                onFailure = { err ->
                    aiErrors[itemKey] = err.message ?: "Xatolik yuz berdi"
                }
            )
        }
    }

    fun triggerGeminiExplanation(itemKey: String, displayPhrase: String, item: CollocationItem) {
        val currentlyExpanded = expandedAiKeys[itemKey] == true
        if (currentlyExpanded) {
            expandedAiKeys[itemKey] = false
            aiResults.remove(itemKey)
            aiErrors.remove(itemKey)
        } else {
            expandedAiKeys.clear()
            expandedAiKeys[itemKey] = true
            generateFreshResult(itemKey, displayPhrase, item)
        }
    }

    val filteredItems = remember(verbGroup, searchQuery) {
        if (searchQuery.isBlank()) {
            verbGroup.items
        } else {
            verbGroup.items.filter { item ->
                item.collocation.contains(searchQuery, ignoreCase = true) ||
                        item.noun.contains(searchQuery, ignoreCase = true) ||
                        item.meaning.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = OxfordBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OxfordBg)
        ) {
            // Top Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Circular Back Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(OxfordButtonWhite)
                            .border(1.dp, OxfordRowBorder, CircleShape)
                            .clickable { onNavigateBack() }
                            .testTag("collocations_back_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = OxfordTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title + Count Pill + Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = verbGroup.verb,
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = OxfordTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OxfordButtonWhite)
                                    .border(1.dp, OxfordRowBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${verbGroup.items.size} ta",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OxfordSelectedTab
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Verb Collocations Lab",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = OxfordTextSecondary
                            )
                        )
                    }

                    // Top Right Cluster Bar (✦ AI, 🔊, 🤍, ⚙️)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OxfordButtonWhite)
                            .border(1.dp, OxfordRowBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // ✦ Gemini AI button
                            IconButton(
                                onClick = { showGeminiDialog = true },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // 🔊 Audio button
                            IconButton(
                                onClick = { ttsHelper?.speak(verbGroup.verb) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Talaffuz",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // 🤍 Favorite heart button
                            IconButton(
                                onClick = { onToggleFavorite() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Sevimli",
                                    tint = if (isFavorite) OxfordHeartActive else OxfordTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // ⚙️ Settings gear button
                            IconButton(
                                onClick = { onNavigateToSettings() },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Sozlamalar",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Birikmalarni qidiring...",
                            color = OxfordTextSecondary,
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Qidirish",
                            tint = OxfordTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Tozalash",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OxfordSearchBarBg,
                        unfocusedContainerColor = OxfordSearchBarBg,
                        focusedBorderColor = OxfordRowBorder,
                        unfocusedBorderColor = OxfordRowBorder,
                        focusedTextColor = OxfordTextPrimary,
                        unfocusedTextColor = OxfordTextPrimary,
                        cursorColor = OxfordSelectedTab
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("collocation_search_input")
                )
            }

            // Collocations List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("collocations_cards_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = filteredItems,
                    key = { index, item -> "${item.collocation}_$index" }
                ) { index, item ->
                    val displayPhrase = item.displayCollocation(verbGroup.verb)
                    val itemKey = "${verbGroup.verb}_${displayPhrase}"
                    val isExpanded = expandedAiKeys[itemKey] == true
                    val isLoading = aiLoading[itemKey] == true
                    val aiResult = aiResults[itemKey]
                    val errorMsg = aiErrors[itemKey]

                    SubtleFadeInListItem(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordCard),
                            border = BorderStroke(
                                if (isExpanded) 1.2.dp else 1.dp,
                                if (isExpanded) OxfordSelectedTab else OxfordBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clickable {
                                    triggerGeminiExplanation(itemKey, displayPhrase, item)
                                }
                                .testTag("collocation_card_$index")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Primary Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = displayPhrase,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.5.sp,
                                            color = OxfordTextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Right Pill Capsule: [✦ AI] Button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(OxfordButtonWhite)
                                            .border(1.dp, OxfordRowBorder, RoundedCornerShape(18.dp))
                                            .clickable {
                                                triggerGeminiExplanation(itemKey, displayPhrase, item)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    color = OxfordSelectedTab,
                                                    modifier = Modifier.size(11.dp),
                                                    strokeWidth = 1.5.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = OxfordSelectedTab,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = "AI",
                                                style = TextStyle(
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OxfordSelectedTab
                                                )
                                            )
                                        }
                                    }
                                }

                                // Inline Result underneath the Collocation (Always fresh, never cached)
                                if (isExpanded) {
                                    HorizontalDivider(
                                        color = OxfordBorder,
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    expandedAiKeys[itemKey] = false
                                                    aiResults.remove(itemKey)
                                                    aiErrors.remove(itemKey)
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Yopish",
                                                    tint = OxfordTextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Dedicated Answer Field
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(OxfordButtonWhite)
                                                .border(1.dp, OxfordRowBorder, RoundedCornerShape(12.dp))
                                                .padding(14.dp)
                                        ) {
                                            if (isLoading) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        color = OxfordSelectedTab,
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "Gemini yangi natija yaratmoqda...",
                                                        color = OxfordTextSecondary,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            } else if (errorMsg != null) {
                                                Text(
                                                    text = errorMsg,
                                                    color = Color(0xFFDC2626),
                                                    fontSize = 12.5.sp
                                                )
                                            } else if (aiResult != null) {
                                                val (englishSentence, uzbekSentence) = sanitizeGeminiText(aiResult)
                                                Column {
                                                    Text(
                                                        text = englishSentence,
                                                        style = TextStyle(
                                                            fontSize = 14.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = OxfordTextPrimary,
                                                            lineHeight = 21.sp
                                                        )
                                                    )
                                                    if (uzbekSentence.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = uzbekSentence,
                                                            style = TextStyle(
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Normal,
                                                                color = OxfordTextSecondary,
                                                                lineHeight = 18.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action Row: Yangi generatsiya qilish + Nusxalash
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    generateFreshResult(itemKey, displayPhrase, item)
                                                },
                                                enabled = !isLoading,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = OxfordSelectedTab,
                                                    disabledContainerColor = OxfordSelectedTab.copy(alpha = 0.5f)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("generate_btn_$index")
                                            ) {
                                                if (isLoading) {
                                                    CircularProgressIndicator(
                                                        color = Color.Black,
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(
                                                    text = "Boshqa misol olish",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            if (aiResult != null && !isLoading) {
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Result", aiResult))
                                                        Toast.makeText(context, "Misol nusxalandi!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(OxfordButtonWhite)
                                                        .border(1.dp, OxfordRowBorder, RoundedCornerShape(10.dp))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Nusxalash",
                                                        tint = OxfordSelectedTab,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showGeminiDialog) {
            GeminiDialog(onDismiss = { showGeminiDialog = false })
        }
    }
}

private fun sanitizeGeminiText(raw: String): Pair<String, String> {
    try {
        val trimmed = raw.trim()
        val jsonStart = trimmed.indexOf('{')
        val jsonEnd = trimmed.lastIndexOf('}')
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            val jsonStr = trimmed.substring(jsonStart, jsonEnd + 1)
            val json = org.json.JSONObject(jsonStr)
            val en = json.optString("english", "").trim()
            val uz = json.optString("uzbek", "").trim()
            if (en.isNotEmpty() && uz.isNotEmpty()) {
                return Pair(en, uz)
            }
        }
    } catch (_: Exception) {}

    val clean = raw.replace("*", "").replace("`", "").trim()
    val lines = clean.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .filter { line ->
            val lower = line.lowercase()
            !lower.startsWith("mana ") && !lower.startsWith("albatta") && !lower.contains("uchun misol")
        }
        .map { line ->
            line.removePrefix("[Inglizcha gap]:").removePrefix("[Inglizcha gap]")
                .removePrefix("[O'zbekcha tarjima]:").removePrefix("[O'zbekcha tarjima]")
                .removePrefix("English:").removePrefix("Inglizcha:")
                .removePrefix("Uzbek:").removePrefix("O'zbekcha:")
                .removePrefix("Tarjima:").removePrefix("Tarjimasi:")
                .removePrefix("Misol:").removePrefix("Example:")
                .removePrefix("1.").removePrefix("2.")
                .removePrefix("-").removePrefix("•")
                .trim().trim('"', '\'')
        }
        .filter { it.isNotEmpty() }

    val english = lines.getOrNull(0) ?: ""
    val uzbek = if (lines.size > 1) lines.drop(1).joinToString("\n") else ""
    return Pair(english, uzbek)
}
