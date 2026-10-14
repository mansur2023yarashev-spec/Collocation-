package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.data.model.AdjectiveGroup
import com.example.data.model.NounGroup
import com.example.data.model.VerbGroup
import com.example.data.parser.AdjectiveJsonParser
import com.example.data.parser.NounJsonParser
import com.example.data.parser.VerbJsonParser
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
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    verbGroups: List<VerbGroup>,
    favorites: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onSelectVerb: (VerbGroup) -> Unit,
    nounGroups: List<NounGroup> = emptyList(),
    nounFavorites: Set<String> = emptySet(),
    onToggleNounFavorite: (String) -> Unit = {},
    onSelectNoun: (NounGroup) -> Unit = {},
    adjectiveGroups: List<AdjectiveGroup> = emptyList(),
    adjectiveFavorites: Set<String> = emptySet(),
    onToggleAdjectiveFavorite: (String) -> Unit = {},
    onSelectAdjective: (AdjectiveGroup) -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onJsonUpdated: (String) -> Unit,
    onNounJsonUpdated: (String) -> Unit = {},
    onAdjectiveJsonUpdated: (String) -> Unit = {},
    ttsHelper: TextToSpeechHelper?,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    nounListState: LazyListState = rememberLazyListState(),
    adjectiveListState: LazyListState = rememberLazyListState(),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedTab: Int = 0,
    onSelectedTabChange: (Int) -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleDarkTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showNounUploadOptionsDialog by remember { mutableStateOf(false) }
    var showNounPasteDialog by remember { mutableStateOf(false) }
    var showNounFormatGuideDialog by remember { mutableStateOf(false) }
    var showAdjectiveUploadOptionsDialog by remember { mutableStateOf(false) }
    var showAdjectivePasteDialog by remember { mutableStateOf(false) }
    var showAdjectiveFormatGuideDialog by remember { mutableStateOf(false) }

    // Direct Verb JSON file picker
    val verbFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }
                    val parsed = VerbJsonParser.parse(content)
                    if (parsed.isNotEmpty()) {
                        val fileName = queryFileName(context, uri)
                        val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("saved_raw_json", content)
                            .putString("saved_file_name", fileName)
                            .putString("saved_date", now)
                            .apply()
                        onJsonUpdated(content)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Muvaffaqiyatli yuklandi: ${parsed.size} ta fe'l!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Faylda fe'llar topilmadi yoki xato format", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fayl ochishda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Direct Noun JSON file picker
    val nounFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }
                    val parsed = NounJsonParser.parse(content)
                    if (parsed.isNotEmpty()) {
                        val fileName = queryFileName(context, uri)
                        val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("saved_noun_raw_json", content)
                            .putString("saved_noun_file_name", fileName)
                            .putString("saved_noun_date", now)
                            .apply()
                        onNounJsonUpdated(content)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Muvaffaqiyatli yuklandi: ${parsed.size} ta ot!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Faylda otlar topilmadi yoki xato format", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fayl ochishda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Direct Adjective JSON file picker
    val adjectiveFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }
                    val parsed = AdjectiveJsonParser.parse(content)
                    if (parsed.isNotEmpty()) {
                        val fileName = queryFileName(context, uri)
                        val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("saved_adjective_raw_json", content)
                            .putString("saved_adjective_file_name", fileName)
                            .putString("saved_adjective_date", now)
                            .apply()
                        onAdjectiveJsonUpdated(content)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Muvaffaqiyatli yuklandi: ${parsed.size} ta sifat!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Faylda sifatlar topilmadi yoki xato format", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fayl ochishda xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Filtered Verbs
    val displayedVerbs = remember(verbGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            verbGroups.sortedBy { it.verb.lowercase() }
        } else {
            verbGroups.filter { group ->
                group.verb.contains(searchQuery, ignoreCase = true) ||
                        group.items.any {
                            it.collocation.contains(searchQuery, ignoreCase = true) ||
                                    it.noun.contains(searchQuery, ignoreCase = true)
                        }
            }.sortedBy { it.verb.lowercase() }
        }
    }

    // Filtered Nouns
    val totalNounCollocations = remember(nounGroups) { nounGroups.sumOf { it.items.size } }
    val displayedNouns = remember(nounGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            nounGroups.sortedBy { it.noun.lowercase() }
        } else {
            nounGroups.filter { group ->
                group.noun.contains(searchQuery, ignoreCase = true) ||
                        group.items.any {
                            it.collocation.contains(searchQuery, ignoreCase = true) ||
                                    it.noun.contains(searchQuery, ignoreCase = true) ||
                                    it.verb.contains(searchQuery, ignoreCase = true) ||
                                    it.resolveVerb(group.noun).contains(searchQuery, ignoreCase = true) ||
                                    it.meaning.contains(searchQuery, ignoreCase = true)
                        }
            }.sortedBy { it.noun.lowercase() }
        }
    }

    // Filtered Adjectives
    val totalAdjectiveCollocations = remember(adjectiveGroups) { adjectiveGroups.sumOf { it.items.size } }
    val displayedAdjectives = remember(adjectiveGroups, searchQuery) {
        if (searchQuery.isBlank()) {
            adjectiveGroups.sortedBy { it.adjective.lowercase() }
        } else {
            adjectiveGroups.filter { group ->
                group.adjective.contains(searchQuery, ignoreCase = true) ||
                        group.items.any {
                            it.collocation.contains(searchQuery, ignoreCase = true) ||
                                    it.noun.contains(searchQuery, ignoreCase = true) ||
                                    it.meaning.contains(searchQuery, ignoreCase = true) ||
                                    it.example.contains(searchQuery, ignoreCase = true)
                        }
            }.sortedBy { it.adjective.lowercase() }
        }
    }

    // Favorite items
    val favoriteVerbs = remember(verbGroups, favorites, searchQuery) {
        val base = verbGroups.filter { favorites.contains(it.verb.lowercase()) }
        if (searchQuery.isBlank()) {
            base.sortedBy { it.verb.lowercase() }
        } else {
            base.filter { it.verb.contains(searchQuery, ignoreCase = true) }.sortedBy { it.verb.lowercase() }
        }
    }

    val favoriteNouns = remember(nounGroups, nounFavorites, searchQuery) {
        val base = nounGroups.filter { nounFavorites.contains(it.noun.lowercase()) }
        if (searchQuery.isBlank()) {
            base.sortedBy { it.noun.lowercase() }
        } else {
            base.filter { it.noun.contains(searchQuery, ignoreCase = true) }.sortedBy { it.noun.lowercase() }
        }
    }

    val favoriteAdjectives = remember(adjectiveGroups, adjectiveFavorites, searchQuery) {
        val base = adjectiveGroups.filter { adjectiveFavorites.contains(it.adjective.lowercase()) }
        if (searchQuery.isBlank()) {
            base.sortedBy { it.adjective.lowercase() }
        } else {
            base.filter { it.adjective.contains(searchQuery, ignoreCase = true) }.sortedBy { it.adjective.lowercase() }
        }
    }

    val availableVerbLetters = remember(displayedVerbs) {
        displayedVerbs.mapNotNull { it.verb.firstOrNull()?.uppercaseChar() }.toSet()
    }

    val availableNounLetters = remember(displayedNouns) {
        displayedNouns.mapNotNull { it.noun.firstOrNull()?.uppercaseChar() }.toSet()
    }

    val availableAdjectiveLetters = remember(displayedAdjectives) {
        displayedAdjectives.mapNotNull { it.adjective.firstOrNull()?.uppercaseChar() }.toSet()
    }

    val edgeMargin = 8.dp
    val cardGap = 8.dp
    val outerRadius = 28.dp
    val itemRowRadius = 22.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OxfordBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(edgeMargin)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(cardGap)
        ) {
            // 1) TOP HEADER CARD
            Card(
                shape = RoundedCornerShape(outerRadius),
                colors = CardDefaults.cardColors(containerColor = OxfordCard),
                border = BorderStroke(1.dp, OxfordBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("header_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    // Title "Alpha" on left + 3 icon buttons on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alpha",
                            style = TextStyle(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = OxfordTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Sparkles Button (✦ AI)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OxfordButtonWhite)
                                    .border(1.dp, OxfordRowBorder, RoundedCornerShape(12.dp))
                                    .clickable { showGeminiDialog = true }
                                    .testTag("home_gemini_icon_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 2. Settings Button (⚙️)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OxfordButtonWhite)
                                    .border(1.dp, OxfordRowBorder, RoundedCornerShape(12.dp))
                                    .clickable { onNavigateToSettings() }
                                    .testTag("home_settings_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Sozlamalar",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 3. Theme Toggle Button (☀️ / 🌙)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OxfordButtonWhite)
                                    .border(1.dp, OxfordRowBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onToggleDarkTheme()
                                        val msg = if (isDarkTheme) "Yorug'lik rejimi yoqildi" else "Tungi rejim yoqildi"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("home_theme_toggle_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                    contentDescription = if (isDarkTheme) "Yorug'lik rejimi" else "Tungi rejim",
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Search Bar according to selected Tab
                    val searchPlaceholder = when (selectedTab) {
                        0 -> "Fe'l qidiring (mas. accept)..."
                        1 -> "Ot qidiring (mas. decision)..."
                        2 -> "Sifat qidiring (mas. strong)..."
                        else -> "Sevimli so'zlardan qidirish..."
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                searchPlaceholder,
                                color = OxfordTextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Qidirish",
                                tint = OxfordTextSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Tozalash",
                                        tint = OxfordTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
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
                            .height(52.dp)
                            .testTag("search_input_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle & JSON upload action per tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subtitleText = when (selectedTab) {
                            0 -> if (verbGroups.isNotEmpty()) "${displayedVerbs.size} ta fe'l" else "JSON fayl yuklanmagan"
                            1 -> if (nounGroups.isNotEmpty()) "${displayedNouns.size} ta ot" else "JSON fayl yuklanmagan"
                            2 -> if (adjectiveGroups.isNotEmpty()) "${displayedAdjectives.size} ta sifat" else "JSON fayl yuklanmagan"
                            else -> "${favoriteVerbs.size + favoriteNouns.size + favoriteAdjectives.size} ta sevimli so'z"
                        }

                        Text(
                            text = subtitleText,
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = OxfordTextSecondary
                            )
                        )

                        // Upload shortcut button for Verb, Noun, or Adjective tab
                        when (selectedTab) {
                            0 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            verbFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("home_upload_verb_json_action")
                                 ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = "JSON yuklash",
                                        tint = OxfordSelectedTab,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (verbGroups.isNotEmpty()) "JSON almashtirish" else "JSON yuklash",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OxfordSelectedTab
                                        )
                                    )
                                }
                            }
                            1 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            showNounUploadOptionsDialog = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("home_upload_noun_json_action")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = "JSON yuklash",
                                        tint = OxfordSelectedTab,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (nounGroups.isNotEmpty()) "JSON almashtirish" else "JSON yuklash",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OxfordSelectedTab
                                        )
                                    )
                                }
                            }
                            2 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            showAdjectiveUploadOptionsDialog = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("home_upload_adjective_json_action")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileUpload,
                                        contentDescription = "JSON yuklash",
                                        tint = OxfordSelectedTab,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (adjectiveGroups.isNotEmpty()) "JSON almashtirish" else "JSON yuklash",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OxfordSelectedTab
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2) MIDDLE LIST CARD
            Card(
                shape = RoundedCornerShape(outerRadius),
                colors = CardDefaults.cardColors(containerColor = OxfordCard),
                border = BorderStroke(1.dp, OxfordBorder),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(outerRadius))
                    .testTag("list_card")
            ) {
                when (selectedTab) {
                    // TAB 0: VERB SECTION
                    0 -> {
                        if (verbGroups.isEmpty()) {
                            // Verb Empty Upload State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(OxfordButtonWhite)
                                            .border(1.dp, OxfordRowBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = OxfordSelectedTab,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Verb JSON faylni yuklang",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = OxfordTextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Fe'llar va birikmalar ro'yxatini ko'rish uchun qurilmangizdagi .json faylni asosiy oynadan yuklang.",
                                        color = OxfordTextSecondary,
                                        fontSize = 13.5.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            verbFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OxfordSelectedTab),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("main_screen_pick_json_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "JSON faylni tanlash",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
                            ) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 6.dp, end = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = displayedVerbs,
                                        key = { it.verb }
                                    ) { group ->
                                        val isFav = favorites.contains(group.verb.lowercase())

                                        SubtleFadeInListItem(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(itemRowRadius),
                                                colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                                border = BorderStroke(1.dp, OxfordRowBorder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .clickable { onSelectVerb(group) }
                                                    .testTag("verb_card_${group.verb}")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // Left: Word + count
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = group.verb,
                                                            style = TextStyle(
                                                                fontWeight = FontWeight.Normal,
                                                                fontSize = 15.sp,
                                                                color = OxfordTextPrimary
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = "${group.items.size}",
                                                            style = TextStyle(
                                                                fontWeight = FontWeight.Normal,
                                                                fontSize = 13.sp,
                                                                color = OxfordTextSecondary
                                                            )
                                                        )
                                                    }

                                                    // Right: Sound, Heart, Chevron
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = { ttsHelper?.speak(group.verb) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                                contentDescription = "Talaffuz",
                                                                tint = OxfordTextSecondary,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { onToggleFavorite(group.verb) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                                contentDescription = "Sevimli",
                                                                tint = if (isFav) OxfordHeartActive else OxfordTextSecondary,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }

                                                        Icon(
                                                            imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Ochish",
                                                            tint = OxfordTextSecondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Slim vertical A–Z alphabet index on the right edge inside the card
                                Column(
                                    modifier = Modifier
                                        .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ('A'..'Z').forEach { letter ->
                                        val isPresent = availableVerbLetters.contains(letter)
                                        Text(
                                            text = letter.toString(),
                                            style = TextStyle(
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isPresent) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isPresent) OxfordTextSecondary else OxfordTextSecondary.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .clickable(enabled = isPresent) {
                                                    val targetIndex = displayedVerbs.indexOfFirst {
                                                        it.verb.startsWith(letter, ignoreCase = true)
                                                    }
                                                    if (targetIndex >= 0) {
                                                        scope.launch {
                                                            listState.animateScrollToItem(targetIndex)
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 0.5.dp, horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: NOUN SECTION
                    1 -> {
                        if (nounGroups.isEmpty()) {
                            // Noun Empty Upload State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(OxfordButtonWhite)
                                            .border(1.dp, OxfordRowBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = OxfordSelectedTab,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Noun JSON faylini yuklang",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = OxfordTextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Asosiy oynada otlar (noun) ro'yxati chiqadi, istalgan otni bosganda unga doir fe'llar collocation ko'rinishida ko'rsatiladi.",
                                        color = OxfordTextSecondary,
                                        fontSize = 13.5.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Button 1: Pick JSON file from device
                                    Button(
                                        onClick = {
                                            nounFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OxfordSelectedTab),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(48.dp)
                                            .testTag("noun_main_pick_json_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "JSON faylni tanlash (Fayl menejeri)",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Button 2: Paste JSON manually
                                    OutlinedButton(
                                        onClick = {
                                            showNounPasteDialog = true
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, OxfordRowBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = OxfordButtonWhite,
                                            contentColor = OxfordTextPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(46.dp)
                                            .testTag("noun_main_paste_json_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = null,
                                            tint = OxfordTextPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "JSON matnini qo'lda kiritish (Paste)",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
                            ) {
                                LazyColumn(
                                    state = nounListState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 6.dp, end = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = displayedNouns,
                                        key = { it.noun }
                                    ) { group ->
                                        SubtleFadeInListItem(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(itemRowRadius),
                                                colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                                border = BorderStroke(1.dp, OxfordRowBorder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                                    .clickable { onSelectNoun(group) }
                                                    .testTag("noun_card_${group.noun}")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = group.noun,
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Serif,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 16.5.sp,
                                                            color = OxfordTextPrimary
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = "Ochish",
                                                        tint = OxfordTextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Slim vertical A–Z alphabet index on the right edge
                                Column(
                                    modifier = Modifier
                                        .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ('A'..'Z').forEach { letter ->
                                        val isPresent = availableNounLetters.contains(letter)
                                        Text(
                                            text = letter.toString(),
                                            style = TextStyle(
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isPresent) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isPresent) OxfordTextSecondary else OxfordTextSecondary.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .clickable(enabled = isPresent) {
                                                    val targetIndex = displayedNouns.indexOfFirst {
                                                        it.noun.startsWith(letter, ignoreCase = true)
                                                    }
                                                    if (targetIndex >= 0) {
                                                        scope.launch {
                                                            nounListState.animateScrollToItem(targetIndex)
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 0.5.dp, horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: ADJECTIVE SECTION
                    2 -> {
                        if (adjectiveGroups.isEmpty()) {
                            // Adjective Empty Upload State
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(OxfordButtonWhite)
                                            .border(1.dp, OxfordRowBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = OxfordSelectedTab,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Adjective JSON faylini yuklang",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = OxfordTextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Asosiy oynada sifatlar (adjective) ro'yxati chiqadi, istalgan sifatni bosganda unga doir otlar (noun) collocation ko'rinishida ko'rsatiladi.",
                                        color = OxfordTextSecondary,
                                        fontSize = 13.5.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Button 1: Pick JSON file from device
                                    Button(
                                        onClick = {
                                            adjectiveFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OxfordSelectedTab),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(48.dp)
                                            .testTag("adjective_main_pick_json_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "JSON faylni tanlash (Fayl menejeri)",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Button 2: Paste JSON manually
                                    OutlinedButton(
                                        onClick = {
                                            showAdjectivePasteDialog = true
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, OxfordRowBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = OxfordButtonWhite,
                                            contentColor = OxfordTextPrimary
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(46.dp)
                                            .testTag("adjective_main_paste_json_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = null,
                                            tint = OxfordTextPrimary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "JSON matnini qo'lda kiritish (Paste)",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
                            ) {
                                LazyColumn(
                                    state = adjectiveListState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentPadding = PaddingValues(top = 2.dp, bottom = 6.dp, end = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = displayedAdjectives,
                                        key = { it.adjective }
                                    ) { group ->
                                        val isFav = adjectiveFavorites.contains(group.adjective.lowercase())

                                        SubtleFadeInListItem(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(itemRowRadius),
                                                colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                                border = BorderStroke(1.dp, OxfordRowBorder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .clickable { onSelectAdjective(group) }
                                                    .testTag("adjective_card_${group.adjective}")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // Left: Word + count
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = group.adjective,
                                                            style = TextStyle(
                                                                fontWeight = FontWeight.Normal,
                                                                fontSize = 15.sp,
                                                                color = OxfordTextPrimary
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = "${group.items.size}",
                                                            style = TextStyle(
                                                                fontWeight = FontWeight.Normal,
                                                                fontSize = 13.sp,
                                                                color = OxfordTextSecondary
                                                            )
                                                        )
                                                    }

                                                    // Right: Sound, Heart, Chevron
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        IconButton(
                                                            onClick = { ttsHelper?.speak(group.adjective) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                                contentDescription = "Talaffuz",
                                                                tint = OxfordTextSecondary,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }

                                                        IconButton(
                                                            onClick = { onToggleAdjectiveFavorite(group.adjective) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                                contentDescription = "Sevimli",
                                                                tint = if (isFav) OxfordHeartActive else OxfordTextSecondary,
                                                                modifier = Modifier.size(17.dp)
                                                            )
                                                        }

                                                        Icon(
                                                            imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Ochish",
                                                            tint = OxfordTextSecondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Slim vertical A–Z alphabet index on the right edge
                                Column(
                                    modifier = Modifier
                                        .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ('A'..'Z').forEach { letter ->
                                        val isPresent = availableAdjectiveLetters.contains(letter)
                                        Text(
                                            text = letter.toString(),
                                            style = TextStyle(
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isPresent) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isPresent) OxfordTextSecondary else OxfordTextSecondary.copy(alpha = 0.35f)
                                            ),
                                            modifier = Modifier
                                                .clickable(enabled = isPresent) {
                                                    val targetIndex = displayedAdjectives.indexOfFirst {
                                                        it.adjective.startsWith(letter, ignoreCase = true)
                                                    }
                                                    if (targetIndex >= 0) {
                                                        scope.launch {
                                                            adjectiveListState.animateScrollToItem(targetIndex)
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 0.5.dp, horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 3: SEVIMLIKLAR (FAVORITES)
                    3 -> {
                        if (favoriteVerbs.isEmpty() && favoriteNouns.isEmpty() && favoriteAdjectives.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = OxfordTextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Sevimli so'zlar yo'q",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = OxfordTextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Verb, Noun yoki Adjective bo'limidagi so'zlar yonidagi yurakcha belgisini bosing.",
                                        fontSize = 13.sp,
                                        color = OxfordTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (favoriteVerbs.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Fe'llar (${favoriteVerbs.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OxfordSelectedTab,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                        )
                                    }
                                    items(favoriteVerbs, key = { "fav_v_${it.verb}" }) { group ->
                                        Card(
                                            shape = RoundedCornerShape(itemRowRadius),
                                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                            border = BorderStroke(1.dp, OxfordRowBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .clickable { onSelectVerb(group) }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = group.verb,
                                                        fontSize = 15.sp,
                                                        color = OxfordTextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${group.items.size}",
                                                        fontSize = 13.sp,
                                                        color = OxfordTextSecondary
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = { ttsHelper?.speak(group.verb) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Talaffuz",
                                                            tint = OxfordTextSecondary,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onToggleFavorite(group.verb) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = "Sevimli",
                                                            tint = OxfordHeartActive,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = OxfordTextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (favoriteNouns.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Otlar (${favoriteNouns.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OxfordSelectedTab,
                                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                        )
                                    }
                                    items(favoriteNouns, key = { "fav_n_${it.noun}" }) { group ->
                                        Card(
                                            shape = RoundedCornerShape(itemRowRadius),
                                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                            border = BorderStroke(1.dp, OxfordRowBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .clickable { onSelectNoun(group) }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = group.noun,
                                                        fontSize = 15.sp,
                                                        color = OxfordTextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${group.items.size}",
                                                        fontSize = 13.sp,
                                                        color = OxfordTextSecondary
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = { ttsHelper?.speak(group.noun) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Talaffuz",
                                                            tint = OxfordTextSecondary,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onToggleNounFavorite(group.noun) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = "Sevimli",
                                                            tint = OxfordHeartActive,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = OxfordTextSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (favoriteAdjectives.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Sifatlar (${favoriteAdjectives.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OxfordSelectedTab,
                                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                        )
                                    }
                                    items(favoriteAdjectives, key = { "fav_a_${it.adjective}" }) { group ->
                                        Card(
                                            shape = RoundedCornerShape(itemRowRadius),
                                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                                            border = BorderStroke(1.dp, OxfordRowBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .clickable { onSelectAdjective(group) }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = group.adjective,
                                                        fontSize = 15.sp,
                                                        color = OxfordTextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${group.items.size}",
                                                        fontSize = 13.sp,
                                                        color = OxfordTextSecondary
                                                    )
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = { ttsHelper?.speak(group.adjective) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Talaffuz",
                                                            tint = OxfordTextSecondary,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { onToggleAdjectiveFavorite(group.adjective) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = "Sevimli",
                                                            tint = OxfordHeartActive,
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = null,
                                                        tint = OxfordTextSecondary,
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

            // 3) BOTTOM NAV CARD (Verb, Noun, Adjective, Sevimliklar)
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = OxfordCard),
                border = BorderStroke(1.dp, OxfordBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("bottom_nav_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 0: Verb
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectedTabChange(0) }
                            .testTag("nav_tab_verb"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Verb",
                            tint = if (selectedTab == 0) OxfordSelectedTab else OxfordTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Verb",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) OxfordSelectedTab else OxfordTextSecondary
                            )
                        )
                    }

                    // Tab 1: Noun
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectedTabChange(1) }
                            .testTag("nav_tab_noun"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = "Noun",
                            tint = if (selectedTab == 1) OxfordSelectedTab else OxfordTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Noun",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) OxfordSelectedTab else OxfordTextSecondary
                            )
                        )
                    }

                    // Tab 2: Adjective
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectedTabChange(2) }
                            .testTag("nav_tab_adjective"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Adjective",
                            tint = if (selectedTab == 2) OxfordSelectedTab else OxfordTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Adjective",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) OxfordSelectedTab else OxfordTextSecondary
                            )
                        )
                    }

                    // Tab 3: Sevimliklar
                    val totalFavorites = favorites.size + nounFavorites.size + adjectiveFavorites.size
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelectedTabChange(3) }
                            .testTag("nav_tab_favorites"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Sevimliklar",
                            tint = if (selectedTab == 3) OxfordSelectedTab else OxfordTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sevimli ($totalFavorites)",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 3) OxfordSelectedTab else OxfordTextSecondary
                            )
                        )
                    }
                }
            }
        }

        if (showGeminiDialog) {
            GeminiDialog(onDismiss = { showGeminiDialog = false })
        }

        if (showNounUploadOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showNounUploadOptionsDialog = false },
                containerColor = OxfordCard,
                title = {
                    Text(
                        text = "Noun JSON yuklash",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OxfordTextPrimary
                        )
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Otlar va ularga doir fe'l collocationlarini yuklash usulini tanlang:",
                            fontSize = 13.sp,
                            color = OxfordTextSecondary
                        )

                        // Option 1: File picker
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNounUploadOptionsDialog = false
                                    nounFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = OxfordSelectedTab,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Fayl menejeri orqali tanlash",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Qurilmadagi .json faylni tanlang",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }

                        // Option 2: Paste text
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNounUploadOptionsDialog = false
                                    showNounPasteDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = OxfordSelectedTab,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "JSON matnini qo'lda kiritish (Paste)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Nusxalangan JSON matnini joylashtiring",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }

                        // Option 3: Guide
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNounUploadOptionsDialog = false
                                    showNounFormatGuideDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Format namunalarini ko'rish",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Qaysi JSON formatlar qo'llab-quvvatlanadi",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showNounUploadOptionsDialog = false }) {
                        Text("Bekor qilish", color = OxfordTextSecondary)
                    }
                }
            )
        }

        if (showNounPasteDialog) {
            NounPasteDialog(
                onDismiss = { showNounPasteDialog = false },
                onSave = { pastedContent ->
                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("saved_noun_raw_json", pastedContent)
                        .putString("saved_noun_file_name", "Qo'lda kiritilgan JSON")
                        .putString("saved_noun_date", now)
                        .apply()
                    onNounJsonUpdated(pastedContent)
                    showNounPasteDialog = false
                    Toast.makeText(context, "Muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showNounFormatGuideDialog) {
            NounFormatGuideDialog(
                onDismiss = { showNounFormatGuideDialog = false },
                onUseSample = { sampleJson ->
                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("saved_noun_raw_json", sampleJson)
                        .putString("saved_noun_file_name", "Namuna JSON")
                        .putString("saved_noun_date", now)
                        .apply()
                    onNounJsonUpdated(sampleJson)
                    showNounFormatGuideDialog = false
                    Toast.makeText(context, "Namuna JSON yuklandi!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAdjectiveUploadOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showAdjectiveUploadOptionsDialog = false },
                containerColor = OxfordCard,
                title = {
                    Text(
                        text = "Adjective JSON yuklash",
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OxfordTextPrimary
                        )
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Sifatlar va ularga doir ot collocationlarini yuklash usulini tanlang:",
                            fontSize = 13.sp,
                            color = OxfordTextSecondary
                        )

                        // Option 1: File picker
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAdjectiveUploadOptionsDialog = false
                                    adjectiveFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = OxfordSelectedTab,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Fayl menejeri orqali tanlash",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Qurilmadagi .json faylni tanlang",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }

                        // Option 2: Paste text
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAdjectiveUploadOptionsDialog = false
                                    showAdjectivePasteDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = OxfordSelectedTab,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "JSON matnini qo'lda kiritish (Paste)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Nusxalangan JSON matnini joylashtiring",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }

                        // Option 3: Guide
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OxfordButtonWhite),
                            border = BorderStroke(1.dp, OxfordRowBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAdjectiveUploadOptionsDialog = false
                                    showAdjectiveFormatGuideDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = OxfordTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Format namunalarini ko'rish",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                    Text(
                                        text = "Qaysi JSON formatlar qo'llab-quvvatlanadi",
                                        fontSize = 12.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAdjectiveUploadOptionsDialog = false }) {
                        Text("Bekor qilish", color = OxfordTextSecondary)
                    }
                }
            )
        }

        if (showAdjectivePasteDialog) {
            AdjectivePasteDialog(
                onDismiss = { showAdjectivePasteDialog = false },
                onSave = { pastedContent ->
                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("saved_adjective_raw_json", pastedContent)
                        .putString("saved_adjective_file_name", "Qo'lda kiritilgan JSON")
                        .putString("saved_adjective_date", now)
                        .apply()
                    onAdjectiveJsonUpdated(pastedContent)
                    showAdjectivePasteDialog = false
                    Toast.makeText(context, "Muvaffaqiyatli saqlandi!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAdjectiveFormatGuideDialog) {
            AdjectiveFormatGuideDialog(
                onDismiss = { showAdjectiveFormatGuideDialog = false },
                onUseSample = { sampleJson ->
                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("saved_adjective_raw_json", sampleJson)
                        .putString("saved_adjective_file_name", "Namuna JSON")
                        .putString("saved_adjective_date", now)
                        .apply()
                    onAdjectiveJsonUpdated(sampleJson)
                    showAdjectiveFormatGuideDialog = false
                    Toast.makeText(context, "Namuna JSON yuklandi!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun NounPasteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val parsed = remember(text) { NounJsonParser.parse(text) }
    val totalCollocations = remember(parsed) { parsed.sumOf { it.items.size } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OxfordCard,
        title = {
            Text(
                text = "JSON matnini kiritish",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Qurilmangizdan nusxalangan JSON matnini quyidagi maydonga joylang:",
                    fontSize = 13.sp,
                    color = OxfordTextSecondary
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            "{\n  \"decision\": [\"make\", \"reach\", \"take\"],\n  \"effort\": [\"make\", \"put in\"]\n}",
                            fontSize = 12.sp,
                            color = OxfordTextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OxfordSearchBarBg,
                        unfocusedContainerColor = OxfordSearchBarBg,
                        focusedBorderColor = OxfordRowBorder,
                        unfocusedBorderColor = OxfordRowBorder,
                        focusedTextColor = OxfordTextPrimary,
                        unfocusedTextColor = OxfordTextPrimary
                    )
                )

                // Live status
                if (text.isNotBlank()) {
                    if (parsed.isNotEmpty()) {
                        Text(
                            text = "✅ Muvaffaqiyatli: ${parsed.size} ta ot, $totalCollocations ta fe'l birikmasi aniqlandi",
                            color = IncomeGreen,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "⚠️ Format xato yoki otlar topilmadi. Qavslar va qo'shtirnoqlarni tekshiring.",
                            color = ExpenseRed,
                            fontSize = 12.5.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        text = """{
  "decision": ["make", "reach", "take", "come to"],
  "effort": ["make", "put in", "spare", "require"],
  "mistake": ["make", "realize", "repeat", "admit"],
  "progress": ["make", "slow down", "monitor"],
  "problem": ["solve", "face", "cause", "tackle", "deal with"]
}"""
                    }) {
                        Text("Namuna joylash", color = OxfordSelectedTab, fontSize = 12.5.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                enabled = parsed.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OxfordSelectedTab,
                    disabledContainerColor = OxfordSelectedTab.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Yuklash va saqlash", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = OxfordTextSecondary)
            }
        }
    )
}

@Composable
fun NounFormatGuideDialog(
    onDismiss: () -> Unit,
    onUseSample: (String) -> Unit
) {
    val sample1 = """{
  "decision": ["make", "reach", "take", "come to"],
  "effort": ["make", "put in", "require"],
  "problem": ["solve", "face", "cause"]
}"""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OxfordCard,
        title = {
            Text(
                text = "Qo'llab-quvvatlanadigan formatlar",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quyidagi ixtiyoriy formatdagi JSON faylni yuklashingiz mumkin:",
                    fontSize = 13.sp,
                    color = OxfordTextSecondary
                )

                Text(
                    text = "1. Oddiy format (Ot -> Fe'llar ro'yxati):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = OxfordTextPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OxfordSearchBarBg)
                        .padding(8.dp)
                ) {
                    Text(
                        text = sample1,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OxfordTextPrimary
                    )
                }

                Text(
                    text = "Ilova avtomatik ravishda otlarni asosiy oynada ro'yxat qiladi, bosilganda esa fe'llarni \"make a decision\", \"reach a decision\" kabi collocation ko'rinishida chiqaradi.",
                    fontSize = 12.sp,
                    color = OxfordTextSecondary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUseSample(sample1) },
                colors = ButtonDefaults.buttonColors(containerColor = OxfordSelectedTab),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Namunani sinab ko'rish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Yopish", color = OxfordTextSecondary)
            }
        }
    )
}

@Composable
fun AdjectivePasteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val parsed = remember(text) { AdjectiveJsonParser.parse(text) }
    val totalCollocations = remember(parsed) { parsed.sumOf { it.items.size } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OxfordCard,
        title = {
            Text(
                text = "Adjective JSON matnini kiritish",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Qurilmangizdan nusxalangan Sifatlar (Adjective) JSON matnini quyidagi maydonga joylang:",
                    fontSize = 13.sp,
                    color = OxfordTextSecondary
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            "{\n  \"strong\": [\"coffee\", \"wind\", \"argument\"],\n  \"heavy\": [\"rain\", \"traffic\", \"burden\"]\n}",
                            fontSize = 12.sp,
                            color = OxfordTextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OxfordSearchBarBg,
                        unfocusedContainerColor = OxfordSearchBarBg,
                        focusedBorderColor = OxfordRowBorder,
                        unfocusedBorderColor = OxfordRowBorder,
                        focusedTextColor = OxfordTextPrimary,
                        unfocusedTextColor = OxfordTextPrimary
                    )
                )

                // Live status
                if (text.isNotBlank()) {
                    if (parsed.isNotEmpty()) {
                        Text(
                            text = "✅ Muvaffaqiyatli: ${parsed.size} ta sifat, $totalCollocations ta ot birikmasi aniqlandi",
                            color = IncomeGreen,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "⚠️ Format xato yoki sifatlar topilmadi. Qavslar va qo'shtirnoqlarni tekshiring.",
                            color = ExpenseRed,
                            fontSize = 12.5.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        text = """{
  "strong": ["coffee", "wind", "argument", "influence", "accent"],
  "heavy": ["rain", "traffic", "smoker", "losses", "burden"],
  "deep": ["breath", "sleep", "trouble", "thought"],
  "bright": ["future", "idea", "color", "student"],
  "great": ["success", "deal", "importance", "opportunity"]
}"""
                    }) {
                        Text("Namuna joylash", color = OxfordSelectedTab, fontSize = 12.5.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                enabled = parsed.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OxfordSelectedTab,
                    disabledContainerColor = OxfordSelectedTab.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Yuklash va saqlash", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish", color = OxfordTextSecondary)
            }
        }
    )
}

@Composable
fun AdjectiveFormatGuideDialog(
    onDismiss: () -> Unit,
    onUseSample: (String) -> Unit
) {
    val sample1 = """{
  "strong": ["coffee", "wind", "argument", "influence"],
  "heavy": ["rain", "traffic", "smoker", "burden"],
  "deep": ["breath", "sleep", "trouble"]
}"""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OxfordCard,
        title = {
            Text(
                text = "Adjective qo'llab-quvvatlanadigan formatlar",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordTextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quyidagi ixtiyoriy formatdagi JSON faylni yuklashingiz mumkin:",
                    fontSize = 13.sp,
                    color = OxfordTextSecondary
                )

                Text(
                    text = "1. Oddiy format (Sifat -> Otlar ro'yxati):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = OxfordTextPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OxfordSearchBarBg)
                        .padding(8.dp)
                ) {
                    Text(
                        text = sample1,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OxfordTextPrimary
                    )
                }

                Text(
                    text = "Ilova avtomatik ravishda sifatlarni (adjective) asosiy oynada ro'yxat qiladi, har bir sifat bosilganda unga doir otlarni (noun) \"strong coffee\", \"strong argument\" kabi collocation ko'rinishida chiqaradi.",
                    fontSize = 12.sp,
                    color = OxfordTextSecondary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUseSample(sample1) },
                colors = ButtonDefaults.buttonColors(containerColor = OxfordSelectedTab),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Namunani sinab ko'rish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Yopish", color = OxfordTextSecondary)
            }
        }
    )
}


private fun queryFileName(context: Context, uri: Uri): String {
    var name = "fayl.json"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex != -1) {
            name = cursor.getString(nameIndex) ?: name
        }
    }
    return name
}
