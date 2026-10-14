package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.OxfordAccentGold
import com.example.ui.theme.OxfordBg
import com.example.ui.theme.OxfordBorder
import com.example.ui.theme.OxfordButtonDark
import com.example.ui.theme.OxfordCard
import com.example.ui.theme.OxfordTextPrimary
import com.example.ui.theme.OxfordTextSecondary
import com.example.util.GeminiHelper
import kotlinx.coroutines.launch

@Composable
fun GeminiDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Prompt, 1: Model, 2: Texnik, 3: Gmail, 4: API

    // Custom prompt state
    var promptInput by remember { mutableStateOf(GeminiHelper.getSavedPrompt(context)) }

    // Model selection state
    var selectedModel by remember { mutableStateOf(GeminiHelper.getSelectedModel(context)) }

    // Technical parameters state (temperature, topP)
    var temperature by remember { mutableFloatStateOf(GeminiHelper.getTemperature(context)) }
    var topP by remember { mutableFloatStateOf(GeminiHelper.getTopP(context)) }

    // Gmail state
    var currentGmail by remember { mutableStateOf(GeminiHelper.getUserGmail(context)) }
    var gmailInput by remember { mutableStateOf(currentGmail) }

    // API Key state
    var isUsingPermanentKey by remember { mutableStateOf(GeminiHelper.isUsingPermanentKey(context)) }
    var effectiveApiKey by remember { mutableStateOf(GeminiHelper.getEffectiveApiKey(context)) }
    var savedApiKey by remember { mutableStateOf(GeminiHelper.getSavedCustomApiKey(context)) }
    var apiKeyInput by remember { mutableStateOf(savedApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var isTestingKey by remember { mutableStateOf(false) }
    var keyTestResult by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = OxfordCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, OxfordBorder),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .testTag("gemini_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Gemini AI Sozlamalari",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "$selectedModel • $currentGmail",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Yopish",
                            tint = OxfordTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Prompt, Model, Texnik, Gmail, API - 5 tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = OxfordButtonDark,
                    contentColor = OxfordAccentGold,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = OxfordAccentGold,
                                height = 2.5.dp
                            )
                        }
                    },
                    divider = {},
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.6.dp, OxfordBorder, RoundedCornerShape(10.dp))
                ) {
                    val tabs = listOf("Prompt", "Model", "Texnik", "Gmail", "API")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        // 0. PROMPT BO'LIMI
                        0 -> {
                            Text(
                                text = "AI uchun Maxsus Prompt:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "Kiritilgan ushbu prompt barcha bo'limlar (Verb, Noun, Adjective) uchun AI ga qo'shimcha maxsus ko'rsatma sifatida beriladi.",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = {
                                    Text(
                                        "Maxsus ko'rsatma yoki prompt kiriting...",
                                        color = OxfordTextSecondary.copy(alpha = 0.5f),
                                        fontSize = 12.5.sp
                                    )
                                },
                                minLines = 4,
                                maxLines = 8,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = OxfordBg,
                                    unfocusedContainerColor = OxfordBg,
                                    focusedBorderColor = OxfordAccentGold,
                                    unfocusedBorderColor = OxfordBorder,
                                    focusedTextColor = OxfordTextPrimary,
                                    unfocusedTextColor = OxfordTextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_prompt_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val trimmed = promptInput.trim()
                                        GeminiHelper.savePrompt(context, trimmed)
                                        Toast.makeText(context, "Prompt saqlandi", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OxfordAccentGold),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("gemini_save_prompt_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Saqlash", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                if (promptInput.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            promptInput = ""
                                            GeminiHelper.savePrompt(context, "")
                                            Toast.makeText(context, "Prompt tozalandi", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(42.dp)
                                    ) {
                                        Text("Tozalash", color = OxfordTextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // 1. MODELLARNI ALMASHTIRISH
                        1 -> {
                            Text(
                                text = "Gemini Modelini tanlang:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "Ilovadagi barcha AI so'rovlari tanlangan model orqali ishlaydi",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val modelList = listOf(
                                Triple(
                                    "gemini-flash-latest",
                                    "Gemini Flash (Men kabi tarjima qiluvchi / Asosiy)",
                                    "Google'ning eng mukammal ko'p tilli modeli. O'zbek tili grammatikasi va nutqini chuqur tushunadi."
                                ),
                                Triple(
                                    "gemini-3.1-pro-preview",
                                    "Gemini 3.1 Pro (Oliy Aql / Eng kuchli)",
                                    "Murakkab tilshunoslik, akademik uslub va chuqur mantiqiy tahlil uchun Google'ning eng kuchli modeli."
                                ),
                                Triple(
                                    "gemini-3.5-flash",
                                    "Gemini 3.5 Flash (Yangi avlod)",
                                    "Google'ning yangi avlod tezkor va aqlli matn modeli."
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                modelList.forEach { (modelId, modelTitle, modelDesc) ->
                                    val isCurrent = selectedModel == modelId

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) OxfordCard else OxfordButtonDark
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isCurrent) 1.2.dp else 0.6.dp,
                                            if (isCurrent) OxfordAccentGold else OxfordBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedModel = modelId
                                                GeminiHelper.saveSelectedModel(context, modelId)
                                                Toast.makeText(context, "Model o'zgartirildi: $modelId", Toast.LENGTH_SHORT).show()
                                            }
                                            .testTag("model_select_$modelId")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Psychology,
                                                        contentDescription = null,
                                                        tint = if (isCurrent) OxfordAccentGold else OxfordTextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = modelTitle,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = if (isCurrent) OxfordAccentGold else OxfordTextPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = modelDesc,
                                                    fontSize = 11.5.sp,
                                                    color = OxfordTextSecondary,
                                                    lineHeight = 15.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            if (isCurrent) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Tanlangan",
                                                    tint = OxfordAccentGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. TEXNIK PARAMETRLAR (ANIQLIK VA XILMA-XILLIK)
                        2 -> {
                            Text(
                                text = "Texnik Aniqlik Sozlamalari:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "Natijalar xilma-xilligi va aniqligi texnik parametrlar orqali boshqariladi",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Temperature Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = OxfordButtonDark),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, OxfordBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Harorat (Temperature)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OxfordTextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(OxfordAccentGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = String.format(java.util.Locale.US, "%.2f", temperature),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OxfordAccentGold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Slider(
                                        value = temperature,
                                        onValueChange = {
                                            temperature = it
                                            GeminiHelper.saveTemperature(context, it)
                                        },
                                        valueRange = 0.10f..1.50f,
                                        steps = 27,
                                        colors = SliderDefaults.colors(
                                            thumbColor = OxfordAccentGold,
                                            activeTrackColor = OxfordAccentGold,
                                            inactiveTrackColor = OxfordBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("0.10 (Bir xil)", fontSize = 10.sp, color = OxfordTextSecondary)
                                        Text("0.85 (Tavsiya)", fontSize = 10.sp, color = OxfordAccentGold, fontWeight = FontWeight.Bold)
                                        Text("1.50 (Har safar yangi)", fontSize = 10.sp, color = OxfordTextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Top-P Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = OxfordButtonDark),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, OxfordBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Top-P (Ehtimollik filtri)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OxfordTextPrimary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(OxfordAccentGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = String.format(java.util.Locale.US, "%.2f", topP),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OxfordAccentGold,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Slider(
                                        value = topP,
                                        onValueChange = {
                                            topP = it
                                            GeminiHelper.saveTopP(context, it)
                                        },
                                        valueRange = 0.50f..1.00f,
                                        steps = 9,
                                        colors = SliderDefaults.colors(
                                            thumbColor = OxfordAccentGold,
                                            activeTrackColor = OxfordAccentGold,
                                            inactiveTrackColor = OxfordBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("0.50 (Tor doira)", fontSize = 10.sp, color = OxfordTextSecondary)
                                        Text("0.95 (Tavsiya)", fontSize = 10.sp, color = OxfordAccentGold, fontWeight = FontWeight.Bold)
                                        Text("1.00 (Keng)", fontSize = 10.sp, color = OxfordTextSecondary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Top-K Info badge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(OxfordBg)
                                    .border(0.6.dp, OxfordBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = OxfordAccentGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Top-K: 40 • Harorat va Top-P parametrlari sozlamasi",
                                        fontSize = 11.sp,
                                        color = OxfordTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Reset to defaults button
                            OutlinedButton(
                                onClick = {
                                    GeminiHelper.resetTechnicalSettings(context)
                                    temperature = GeminiHelper.DEFAULT_TEMPERATURE
                                    topP = GeminiHelper.DEFAULT_TOP_P
                                    Toast.makeText(context, "Standart texnik parametrlar tiklandi", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text(
                                    "Standart texnik qiymatlarga qaytarish (0.40 / 0.90)",
                                    fontSize = 12.sp,
                                    color = OxfordAccentGold
                                )
                            }
                        }

                        // 3. GMAIL ALMASHTIRISH
                        3 -> {
                            Text(
                                text = "Foydalanuvchi Gmail manzili:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "Ilovadagi faoliyat va hisobni bog'lash uchun Gmail",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = gmailInput,
                                onValueChange = { gmailInput = it },
                                placeholder = { Text("misol@gmail.com", color = OxfordTextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = OxfordAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = OxfordBg,
                                    unfocusedContainerColor = OxfordBg,
                                    focusedBorderColor = OxfordAccentGold,
                                    unfocusedBorderColor = OxfordBorder,
                                    focusedTextColor = OxfordTextPrimary,
                                    unfocusedTextColor = OxfordTextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_gmail_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Default email shortcut button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E294B))
                                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable { gmailInput = GeminiHelper.DEFAULT_GMAIL }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Asosiy Gmail ga qaytarish",
                                        fontSize = 11.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val trimmed = gmailInput.trim()
                                    if (trimmed.isEmpty() || !trimmed.contains("@")) {
                                        Toast.makeText(context, "Yaroqli Gmail manzil kiriting", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    GeminiHelper.saveUserGmail(context, trimmed)
                                    currentGmail = trimmed
                                    Toast.makeText(context, "Gmail almashtirildi: $trimmed", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OxfordAccentGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("gemini_save_gmail_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gmailni Saqlash / Almashtirish", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 4. API KIRITISH
                        4 -> {
                            val hasPermanent = GeminiHelper.hasPermanentApiKey()
                            val permanentKey = GeminiHelper.getPermanentApiKey()

                            // Status badge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (effectiveApiKey.isNotEmpty()) Color(0xFF0D3320) else Color(0xFF3B2310))
                                    .border(
                                        0.7.dp,
                                        if (effectiveApiKey.isNotEmpty()) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (effectiveApiKey.isNotEmpty()) Color(0xFF10B981) else Color(0xFFF59E0B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isUsingPermanentKey) {
                                                "✓ Doimiy API integratsiyasi faol"
                                            } else if (effectiveApiKey.isNotEmpty()) {
                                                "✓ Shaxsiy API kalit faol"
                                            } else {
                                                "⚠️ API kalit kiritilmagan"
                                            },
                                            color = if (effectiveApiKey.isNotEmpty()) Color(0xFF6EE7B7) else Color(0xFFFDE68A),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (isUsingPermanentKey) {
                                            "Ilovaga doimiy API kalit o'rnatilgan. Har safar kalit kiritish yoki almashtirish shart emas."
                                        } else {
                                            "Foydalanuvchi tomonidan kiritilgan maxsus API kalit ishlatilmoqda."
                                        },
                                        fontSize = 11.5.sp,
                                        color = OxfordTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Permanent key card if available
                            if (hasPermanent) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = OxfordButtonDark),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, OxfordBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Doimiy tizim kaliti",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = OxfordTextPrimary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1E3A8A).copy(alpha = 0.5f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isUsingPermanentKey) "Hozir ishlatilmoqda" else "Doimiy zaxira",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF93C5FD),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = GeminiHelper.getMaskedKey(permanentKey),
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = OxfordAccentGold
                                        )
                                        if (!isUsingPermanentKey) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    GeminiHelper.resetToPermanentKey(context)
                                                    savedApiKey = ""
                                                    apiKeyInput = ""
                                                    effectiveApiKey = GeminiHelper.getEffectiveApiKey(context)
                                                    isUsingPermanentKey = true
                                                    keyTestResult = null
                                                    Toast.makeText(context, "Doimiy kalitga qaytarildi", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                            ) {
                                                Text("Doimiy kalitga qaytarish", fontSize = 11.5.sp, color = OxfordAccentGold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Text(
                                text = "Shaxsiy API kalit (Ixtiyoriy):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OxfordTextPrimary
                            )
                            Text(
                                text = "Boshqa kalit ishlatmoqchi bo'lsangiz kiriting, aks holda doimiy kalit ishlaydi",
                                fontSize = 11.5.sp,
                                color = OxfordTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("AIzaSy...", color = OxfordTextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = OxfordAccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Ko'rsatish",
                                            tint = OxfordTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = OxfordBg,
                                    unfocusedContainerColor = OxfordBg,
                                    focusedBorderColor = OxfordAccentGold,
                                    unfocusedBorderColor = OxfordBorder,
                                    focusedTextColor = OxfordTextPrimary,
                                    unfocusedTextColor = OxfordTextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_api_key_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        GeminiHelper.saveCustomApiKey(context, apiKeyInput)
                                        savedApiKey = apiKeyInput
                                        effectiveApiKey = GeminiHelper.getEffectiveApiKey(context)
                                        isUsingPermanentKey = GeminiHelper.isUsingPermanentKey(context)
                                        keyTestResult = null
                                        Toast.makeText(context, if (apiKeyInput.isBlank()) "Doimiy kalit tanlandi" else "API kalit saqlandi", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OxfordAccentGold),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("gemini_save_key_btn")
                                ) {
                                    Text("Saqlash", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val testKey = apiKeyInput.trim().ifEmpty { GeminiHelper.getEffectiveApiKey(context) }
                                        if (testKey.isEmpty()) {
                                            Toast.makeText(context, "API kaliti mavjud emas", Toast.LENGTH_SHORT).show()
                                            return@OutlinedButton
                                        }
                                        isTestingKey = true
                                        keyTestResult = null
                                        scope.launch {
                                            val res = GeminiHelper.generateContent(
                                                context = context,
                                                prompt = "Hello! Test connection.",
                                                apiKeyOverride = testKey
                                            )
                                            isTestingKey = false
                                            res.fold(
                                                onSuccess = {
                                                    keyTestResult = "✓ Kalit muvaffaqiyatli ishlayapti!"
                                                    if (apiKeyInput.isNotBlank()) {
                                                        GeminiHelper.saveCustomApiKey(context, testKey)
                                                        savedApiKey = testKey
                                                        effectiveApiKey = testKey
                                                        isUsingPermanentKey = false
                                                    }
                                                },
                                                onFailure = { err ->
                                                    keyTestResult = "❌ Xatolik: ${err.message}"
                                                }
                                            )
                                        }
                                    },
                                    enabled = !isTestingKey,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("gemini_test_key_btn")
                                ) {
                                    if (isTestingKey) {
                                        CircularProgressIndicator(
                                            color = OxfordAccentGold,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Tekshirish", color = OxfordAccentGold, fontSize = 13.sp)
                                    }
                                }
                            }

                            AnimatedVisibility(visible = keyTestResult != null) {
                                Text(
                                    text = keyTestResult ?: "",
                                    color = if (keyTestResult?.startsWith("✓") == true) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
