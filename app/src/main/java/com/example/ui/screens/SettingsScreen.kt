package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.AdjectiveJsonParser
import com.example.data.parser.NounJsonParser
import com.example.data.parser.VerbJsonParser
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.OxfordAccentGold
import com.example.ui.theme.OxfordAccentGoldLight
import com.example.ui.theme.OxfordBg
import com.example.ui.theme.OxfordBorder
import com.example.ui.theme.OxfordButtonDark
import com.example.ui.theme.OxfordCard
import com.example.ui.theme.OxfordTextPrimary
import com.example.ui.theme.OxfordTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LoadedJsonDetails(
    val fileName: String,
    val fileSize: String,
    val rawJson: String,
    val formattedJson: String,
    val isValid: Boolean,
    val itemCount: Int = 0,
    val itemType: String = "fe'l",
    val totalCollocations: Int = 0,
    val errorMessage: String? = null,
    val loadedAt: String = ""
)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onJsonUpdated: (String) -> Unit,
    onNounJsonUpdated: (String) -> Unit = {},
    onAdjectiveJsonUpdated: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Verb JSON state
    var verbJsonDetails by remember {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        val savedRaw = prefs.getString("saved_raw_json", null)
        val savedName = prefs.getString("saved_file_name", null)
        val savedDate = prefs.getString("saved_date", null)

        val initial = if (!savedRaw.isNullOrBlank() && !savedName.isNullOrBlank()) {
            parseAndAnalyzeVerbJson(savedName, "${savedRaw.toByteArray().size} bayt", savedRaw, savedDate ?: "")
        } else {
            null
        }
        mutableStateOf<LoadedJsonDetails?>(initial)
    }

    // Noun JSON state
    var nounJsonDetails by remember {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        val savedRaw = prefs.getString("saved_noun_raw_json", null)
        val savedName = prefs.getString("saved_noun_file_name", null)
        val savedDate = prefs.getString("saved_noun_date", null)

        val initial = if (!savedRaw.isNullOrBlank() && !savedName.isNullOrBlank()) {
            parseAndAnalyzeNounJson(savedName, "${savedRaw.toByteArray().size} bayt", savedRaw, savedDate ?: "")
        } else {
            null
        }
        mutableStateOf<LoadedJsonDetails?>(initial)
    }

    // Adjective JSON state
    var adjectiveJsonDetails by remember {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        val savedRaw = prefs.getString("saved_adjective_raw_json", null)
        val savedName = prefs.getString("saved_adjective_file_name", null)
        val savedDate = prefs.getString("saved_adjective_date", null)

        val initial = if (!savedRaw.isNullOrBlank() && !savedName.isNullOrBlank()) {
            parseAndAnalyzeAdjectiveJson(savedName, "${savedRaw.toByteArray().size} bayt", savedRaw, savedDate ?: "")
        } else {
            null
        }
        mutableStateOf<LoadedJsonDetails?>(initial)
    }

    var isVerbLoading by remember { mutableStateOf(false) }
    var isNounLoading by remember { mutableStateOf(false) }
    var isAdjectiveLoading by remember { mutableStateOf(false) }

    fun saveVerbJson(name: String, raw: String, date: String) {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_raw_json", raw)
            .putString("saved_file_name", name)
            .putString("saved_date", date)
            .apply()
        onJsonUpdated(raw)
    }

    fun clearVerbJson() {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_raw_json")
            .remove("saved_file_name")
            .remove("saved_date")
            .apply()
        verbJsonDetails = null
        onJsonUpdated("")
        Toast.makeText(context, "Verb JSON fayli o'chirildi", Toast.LENGTH_SHORT).show()
    }

    fun saveNounJson(name: String, raw: String, date: String) {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_noun_raw_json", raw)
            .putString("saved_noun_file_name", name)
            .putString("saved_noun_date", date)
            .apply()
        onNounJsonUpdated(raw)
    }

    fun clearNounJson() {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_noun_raw_json")
            .remove("saved_noun_file_name")
            .remove("saved_noun_date")
            .apply()
        nounJsonDetails = null
        onNounJsonUpdated("")
        Toast.makeText(context, "Noun JSON fayli o'chirildi", Toast.LENGTH_SHORT).show()
    }

    fun saveAdjectiveJson(name: String, raw: String, date: String) {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("saved_adjective_raw_json", raw)
            .putString("saved_adjective_file_name", name)
            .putString("saved_adjective_date", date)
            .apply()
        onAdjectiveJsonUpdated(raw)
    }

    fun clearAdjectiveJson() {
        val prefs = context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_adjective_raw_json")
            .remove("saved_adjective_file_name")
            .remove("saved_adjective_date")
            .apply()
        adjectiveJsonDetails = null
        onAdjectiveJsonUpdated("")
        Toast.makeText(context, "Adjective JSON fayli o'chirildi", Toast.LENGTH_SHORT).show()
    }

    val verbFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isVerbLoading = true
            scope.launch {
                try {
                    val (fileName, fileSize) = queryFileMeta(context, uri)
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }

                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val analyzed = parseAndAnalyzeVerbJson(fileName, fileSize, content, now)
                    verbJsonDetails = analyzed

                    if (analyzed.isValid) {
                        saveVerbJson(fileName, content, now)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Muvaffaqiyatli yuklandi: ${analyzed.itemCount} ta fe'l!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Fayl to'g'ri JSON formatida emas!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    isVerbLoading = false
                }
            }
        }
    }

    val nounFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isNounLoading = true
            scope.launch {
                try {
                    val (fileName, fileSize) = queryFileMeta(context, uri)
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }

                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val analyzed = parseAndAnalyzeNounJson(fileName, fileSize, content, now)
                    nounJsonDetails = analyzed

                    if (analyzed.isValid) {
                        saveNounJson(fileName, content, now)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Muvaffaqiyatli yuklandi: ${analyzed.itemCount} ta ot!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Fayl to'g'ri JSON formatida emas!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    isNounLoading = false
                }
            }
        }
    }

    val adjectiveFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isAdjectiveLoading = true
            scope.launch {
                try {
                    val (fileName, fileSize) = queryFileMeta(context, uri)
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                        } ?: ""
                    }

                    val now = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
                    val analyzed = parseAndAnalyzeAdjectiveJson(fileName, fileSize, content, now)
                    adjectiveJsonDetails = analyzed

                    if (analyzed.isValid) {
                        saveAdjectiveJson(fileName, content, now)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Muvaffaqiyatli yuklandi: ${analyzed.itemCount} ta sifat!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Fayl to'g'ri JSON formatida emas!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Xatolik: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    isAdjectiveLoading = false
                }
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(OxfordButtonDark)
                        .border(1.dp, OxfordBorder, CircleShape)
                        .clickable { onNavigateBack() }
                        .testTag("settings_back_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = OxfordTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Sozlamalar",
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = OxfordTextPrimary
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1) VERB JSON CARD
                item {
                    JsonSectionCard(
                        title = "Verb (Fe'llar) JSON fayli",
                        subtitle = "Fe'llar va ularning birikmalari bazasi",
                        details = verbJsonDetails,
                        isLoading = isVerbLoading,
                        onPickFile = {
                            verbFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        onDeleteFile = { clearVerbJson() },
                        onCopyJson = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Verb JSON", verbJsonDetails?.rawJson ?: "")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Nusxa olindi", Toast.LENGTH_SHORT).show()
                        },
                        uploadTestTag = "pick_verb_json_file_btn"
                    )
                }

                // 2) NOUN JSON CARD
                item {
                    JsonSectionCard(
                        title = "Noun (Otlar) JSON fayli",
                        subtitle = "Otlar va ularning birikmalari bazasi",
                        details = nounJsonDetails,
                        isLoading = isNounLoading,
                        onPickFile = {
                            nounFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        onDeleteFile = { clearNounJson() },
                        onCopyJson = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Noun JSON", nounJsonDetails?.rawJson ?: "")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Nusxa olindi", Toast.LENGTH_SHORT).show()
                        },
                        uploadTestTag = "pick_noun_json_file_btn"
                    )
                }

                // 3) ADJECTIVE JSON CARD
                item {
                    JsonSectionCard(
                        title = "Adjective (Sifatlar) JSON fayli",
                        subtitle = "Sifatlar va ularga doir ot birikmalari bazasi",
                        details = adjectiveJsonDetails,
                        isLoading = isAdjectiveLoading,
                        onPickFile = {
                            adjectiveFilePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        onDeleteFile = { clearAdjectiveJson() },
                        onCopyJson = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Adjective JSON", adjectiveJsonDetails?.rawJson ?: "")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Nusxa olindi", Toast.LENGTH_SHORT).show()
                        },
                        uploadTestTag = "pick_adjective_json_file_btn"
                    )
                }

                // 4) HELP CARD
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = OxfordCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OxfordBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = OxfordAccentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "JSON Fayl Tuzilishi Namunalari",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = OxfordTextPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• Verb JSON (Fe'llar):\n" +
                                        "{\n" +
                                        "  \"abandon\": [\"an allegiance\", \"an attempt\"],\n" +
                                        "  \"make\": [\"a decision\", \"money\"]\n" +
                                        "}\n\n" +
                                        "• Noun JSON (Otlar):\n" +
                                        "{\n" +
                                        "  \"decision\": [\"make a decision\", \"reach a decision\"],\n" +
                                        "  \"effort\": [\"make an effort\", \"put in effort\"]\n" +
                                        "}\n\n" +
                                        "• Adjective JSON (Sifatlar):\n" +
                                        "{\n" +
                                        "  \"strong\": [\"coffee\", \"wind\", \"argument\", \"accent\"],\n" +
                                        "  \"heavy\": [\"rain\", \"traffic\", \"smoker\", \"burden\"],\n" +
                                        "  \"deep\": [\"breath\", \"thought\", \"trouble\"]\n" +
                                        "}\n\n" +
                                        "Sifatlar oynasida sifatlar ro'yxati chiqadi, har biri tanlanganda unga doir otlar collocation ko'rinishida ko'rsatiladi.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = OxfordTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonSectionCard(
    title: String,
    subtitle: String,
    details: LoadedJsonDetails?,
    isLoading: Boolean,
    onPickFile: () -> Unit,
    onDeleteFile: () -> Unit,
    onCopyJson: () -> Unit,
    uploadTestTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OxfordCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, OxfordBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OxfordButtonDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = OxfordAccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = OxfordTextPrimary
                        )
                    )
                    Text(
                        text = subtitle,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            color = OxfordTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPickFile,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OxfordAccentGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag(uploadTestTag)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yuklanmoqda...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (details != null) "Boshqa fayl tanlash" else "JSON faylni tanlash",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Preview details if loaded
            if (details != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OxfordButtonDark)
                        .border(1.dp, OxfordBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (details.isValid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (details.isValid) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = details.fileName,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = OxfordTextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${details.fileSize} • ${details.loadedAt}",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 11.sp,
                                            color = OxfordTextSecondary
                                        )
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = onCopyJson,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Nusxalash",
                                        tint = OxfordTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onDeleteFile,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "O'chirish",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (details.isValid) IncomeGreen.copy(alpha = 0.2f)
                                        else ExpenseRed.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (details.isValid) "To'g'ri JSON" else "Xato format",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (details.isValid) IncomeGreen else ExpenseRed
                                    )
                                )
                            }

                            if (details.isValid) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OxfordBg)
                                        .border(0.6.dp, OxfordBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${details.itemCount} ta ${details.itemType}",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OxfordAccentGoldLight
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OxfordBg)
                                        .border(0.6.dp, OxfordBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${details.totalCollocations} ta birikma",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 10.5.sp,
                                            color = OxfordTextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OxfordBg)
                                .border(0.6.dp, OxfordBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = details.formattedJson,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = OxfordAccentGold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseAndAnalyzeVerbJson(name: String, size: String, content: String, date: String): LoadedJsonDetails {
    return try {
        val trimmed = content.trim().removePrefix("\uFEFF").trim()
        val formatted = when {
            trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
            trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
            else -> throw IllegalArgumentException("Fayl JSON emas")
        }

        val verbGroups = VerbJsonParser.parse(content)
        val totalCollocations = verbGroups.sumOf { it.items.size }

        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = formatted,
            isValid = verbGroups.isNotEmpty(),
            itemCount = verbGroups.size,
            itemType = "fe'l",
            totalCollocations = totalCollocations,
            loadedAt = date
        )
    } catch (e: Exception) {
        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = content.take(500),
            isValid = false,
            errorMessage = e.message ?: "Xatolik",
            itemCount = 0,
            itemType = "fe'l",
            totalCollocations = 0,
            loadedAt = date
        )
    }
}

private fun parseAndAnalyzeNounJson(name: String, size: String, content: String, date: String): LoadedJsonDetails {
    return try {
        val trimmed = content.trim().removePrefix("\uFEFF").trim()
        val formatted = when {
            trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
            trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
            else -> throw IllegalArgumentException("Fayl JSON emas")
        }

        val nounGroups = NounJsonParser.parse(content)
        val totalCollocations = nounGroups.sumOf { it.items.size }

        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = formatted,
            isValid = nounGroups.isNotEmpty(),
            itemCount = nounGroups.size,
            itemType = "ot",
            totalCollocations = totalCollocations,
            loadedAt = date
        )
    } catch (e: Exception) {
        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = content.take(500),
            isValid = false,
            errorMessage = e.message ?: "Xatolik",
            itemCount = 0,
            itemType = "ot",
            totalCollocations = 0,
            loadedAt = date
        )
    }
}

private fun parseAndAnalyzeAdjectiveJson(name: String, size: String, content: String, date: String): LoadedJsonDetails {
    return try {
        val trimmed = content.trim().removePrefix("\uFEFF").trim()
        val formatted = when {
            trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
            trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
            else -> throw IllegalArgumentException("Fayl JSON emas")
        }

        val adjectiveGroups = AdjectiveJsonParser.parse(content)
        val totalCollocations = adjectiveGroups.sumOf { it.items.size }

        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = formatted,
            isValid = adjectiveGroups.isNotEmpty(),
            itemCount = adjectiveGroups.size,
            itemType = "sifat",
            totalCollocations = totalCollocations,
            loadedAt = date
        )
    } catch (e: Exception) {
        LoadedJsonDetails(
            fileName = name,
            fileSize = size,
            rawJson = content,
            formattedJson = content.take(500),
            isValid = false,
            errorMessage = e.message ?: "Xatolik",
            itemCount = 0,
            itemType = "sifat",
            totalCollocations = 0,
            loadedAt = date
        )
    }
}

private fun queryFileMeta(context: Context, uri: Uri): Pair<String, String> {
    var name = "fayl.json"
    var sizeStr = "Noma'lum"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex) ?: name
            }
            if (sizeIndex != -1) {
                val size = cursor.getLong(sizeIndex)
                sizeStr = if (size > 1024) "${size / 1024} KB" else "$size bayt"
            }
        }
    }
    return Pair(name, sizeStr)
}
