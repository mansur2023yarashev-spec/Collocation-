package com.example.util

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val PREFS_NAME = "collocations_app_prefs"
    private const val KEY_CUSTOM_API = "custom_gemini_api_key"
    private const val KEY_USER_GMAIL = "user_gmail"
    private const val KEY_SELECTED_MODEL = "selected_gemini_model"
    private const val KEY_SAVED_PROMPT = "saved_gemini_prompt"
    private const val KEY_TEMPERATURE = "technical_temperature"
    private const val KEY_TOP_P = "technical_top_p"
    const val DEFAULT_TEMPERATURE = 0.40f
    const val DEFAULT_TOP_P = 0.90f
    const val DEFAULT_TOP_K = 40
    const val DEFAULT_GMAIL = "mansur2023yarashev@gmail.com"

    val AVAILABLE_MODELS = listOf(
        "gemini-flash-latest",
        "gemini-3.1-pro-preview",
        "gemini-3.5-flash"
    )
    const val DEFAULT_MODEL = "gemini-flash-latest"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(7, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // Ensure no caching headers on every network request
                val request = chain.request().newBuilder()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun getSelectedModel(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SELECTED_MODEL, null)
        if (saved == null || saved.contains("flash-lite") || !AVAILABLE_MODELS.contains(saved)) {
            prefs.edit().putString(KEY_SELECTED_MODEL, DEFAULT_MODEL).apply()
            return DEFAULT_MODEL
        }
        return saved
    }

    fun saveSelectedModel(context: Context, model: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_MODEL, model.trim()).apply()
    }

    fun getSavedPrompt(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_PROMPT, "") ?: ""
    }

    fun savePrompt(context: Context, prompt: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove(KEY_SAVED_PROMPT).apply()
        } else {
            prefs.edit().putString(KEY_SAVED_PROMPT, trimmed).apply()
        }
    }

    fun getTemperature(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
    }

    fun saveTemperature(context: Context, temp: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
    }

    fun getTopP(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_TOP_P, DEFAULT_TOP_P)
    }

    fun saveTopP(context: Context, topP: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_TOP_P, topP).apply()
    }

    fun resetTechnicalSettings(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
            .putFloat(KEY_TOP_P, DEFAULT_TOP_P)
            .apply()
    }

    fun clearSavedPrompts(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_SAVED_PROMPT)
            .apply()
    }

    fun getPermanentApiKey(): String {
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
                buildKey
            } else {
                ""
            }
        } catch (e: Throwable) {
            ""
        }
    }

    fun hasPermanentApiKey(): Boolean = getPermanentApiKey().isNotEmpty()

    fun getMaskedKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.length <= 8) return "••••••••"
        return "${trimmed.take(6)}...${trimmed.takeLast(4)}"
    }

    fun isUsingPermanentKey(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(KEY_CUSTOM_API, "")?.trim() ?: ""
        return customKey.isEmpty() && hasPermanentApiKey()
    }

    fun resetToPermanentKey(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CUSTOM_API).apply()
    }

    fun getEffectiveApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(KEY_CUSTOM_API, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) {
            return customKey
        }
        return getPermanentApiKey()
    }

    fun getSavedCustomApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_API, "") ?: ""
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            prefs.edit().remove(KEY_CUSTOM_API).apply()
        } else {
            prefs.edit().putString(KEY_CUSTOM_API, trimmed).apply()
        }
    }

    fun getUserGmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_GMAIL, DEFAULT_GMAIL) ?: DEFAULT_GMAIL
    }

    fun saveUserGmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_GMAIL, email.trim()).apply()
    }

    /**
     * Generates content from Gemini with temperature calibrated for high accuracy and fluency.
     * Integrates permanent API key with resilient model fallback (e.g. Gemini 2.5 Flash / Flash Latest).
     */
    suspend fun generateContent(
        context: Context,
        prompt: String,
        systemInstruction: String? = null,
        apiKeyOverride: String? = null,
        temperatureOverride: Float? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKeyOverride?.trim()?.takeIf { it.isNotEmpty() } ?: getEffectiveApiKey(context)
        if (key.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API kaliti topilmadi. Iltimos, API kalitini tekshiring.")
            )
        }

        val primaryModel = getSelectedModel(context)
        val candidateModels = mutableListOf(primaryModel)
        for (m in AVAILABLE_MODELS) {
            if (!candidateModels.contains(m)) {
                candidateModels.add(m)
            }
        }

        var lastErrorMsg = "Gemini serveriga ulanishda xatolik"

        for (model in candidateModels) {
            try {
                val endpoint = "$BASE_URL/$model:generateContent?key=$key"

                val jsonPayload = JSONObject().apply {
                    if (!systemInstruction.isNullOrBlank()) {
                        val sysObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", systemInstruction)
                                })
                            }
                            put("parts", parts)
                        }
                        put("system_instruction", sysObj)
                    }

                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)

                    val effectiveTemp = (temperatureOverride ?: getTemperature(context)).toDouble()
                    val genConfig = JSONObject().apply {
                        put("temperature", effectiveTemp)
                        put("seed", (System.currentTimeMillis() % 1000000L).toInt())
                        put("responseMimeType", "application/json")
                        val topP = getTopP(context).toDouble()
                        if (topP in 0.01..1.0) {
                            put("topP", topP)
                        }
                        put("maxOutputTokens", 1024)
                    }
                    put("generationConfig", genConfig)
                }

                val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(endpoint)
                    .post(requestBody)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("X-Request-Nonce", UUID.randomUUID().toString())
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errJson = JSONObject(responseBody)
                        val errorObj = errJson.optJSONObject("error")
                        errorObj?.optString("message") ?: "Xatolik: ${response.code}"
                    } catch (e: Exception) {
                        "So'rovda xatolik (${response.code})"
                    }
                    lastErrorMsg = errorMsg

                    // If model not found (404) or high demand (503/500), try fallback model
                    if (response.code == 404 || response.code == 503 || response.code == 500) {
                        kotlinx.coroutines.delay(400)
                        continue
                    } else {
                        // Auth or quota errors (400, 401, 403)
                        return@withContext Result.failure(RuntimeException(errorMsg))
                    }
                }

                val resJson = JSONObject(responseBody)
                val candidates = resJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCand = candidates.getJSONObject(0)
                    val content = firstCand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext Result.success(text)
                        }
                    }
                }

                lastErrorMsg = "Gemini bo'sh javob qaytardi."
            } catch (e: Exception) {
                lastErrorMsg = e.message ?: "Aloqa uzildi"
            }
        }

        Result.failure(RuntimeException(lastErrorMsg))
    }

    suspend fun explainCollocation(
        context: Context,
        category: String = "",
        verb: String = "",
        collocation: String,
        noun: String = "",
        adjective: String = "",
        meaning: String = "",
        previousExamples: List<String> = emptyList()
    ): Result<String> {
        val userCustomPrompt = getSavedPrompt(context).trim()

        val detectedCategory = when {
            category.isNotBlank() -> category.lowercase()
            adjective.isNotBlank() -> "adjective"
            noun.isNotBlank() && verb.isBlank() -> "noun"
            verb.isNotBlank() -> "verb"
            else -> "verb"
        }

        val prompt = buildString {
            when (detectedCategory) {
                "verb" -> {
                    appendLine("VAZIFA: FE'L (VERB) BO'LIMIDAGI COLLOCATION UCHUN MISOL GAP VA MUKAMMAL TARJIMA.")
                    appendLine("Asosiy fe'l: \"$verb\"")
                    if (noun.isNotBlank()) appendLine("Bog'liq ot: \"$noun\"")
                    appendLine("Collocation iborasi: \"$collocation\"")
                    if (meaning.isNotBlank()) appendLine("Asl ma'nosi: \"$meaning\"")
                    appendLine()
                    appendLine("QAT'IY TALABLAR:")
                    appendLine("1. SO'Z TARTIBI VA TURLANISH:")
                    appendLine("   - Iborani lug'atdagi xom ko'rinishda gapga ko'chirma!")
                    appendLine("   - Gap mazmuniga mos tabiiy zamon tanla (Past Simple, Present Perfect, Continuous yoki Modals) va fe'lni to'g'ri turla.")
                    appendLine("   - Birikmadagi \"sth\" (something) yoki \"sb\" (somebody) qisqartmalarini mutlaqo ishlatma! Ularning o'rniga gap mazmuniga to'la mos keluvchi aniq, real ob'ekt qo'y.")
                    appendLine("   - Kerakli artikllar (a/an/the) va egalik olmoshlarini (my/his/their) tabiiy joylashtir.")
                    appendLine("2. YUQORI DARAJADAGI O'ZBEKCHA TARJIMA:")
                    appendLine("   - So'zma-so'z (kalka) tarjima qat'iyan man etiladi!")
                    appendLine("   - O'zbek tili sintaksisiga qat'iy amal qil: KESIM (FE'L) GAPNING ENG OXIRIDA bo'lishi shart.")
                    appendLine("   - Zamon uyg'unligi: Inglizcha zamon o'zbekcha zamon qo'shimchalarida (-di, -gan, -yotgan edi, -moqda) 100% aniq aks etsin.")
                    appendLine("   - Kelishiklarni to'g'ri boshqar: tushum (-ni), jo'nalish (-ga), chiqish (-dan) qo'shimchalarini fe'l ma'nosiga mosla.")
                    appendLine()
                    appendLine("IDEAL NAMUNALAR (FEW-SHOT):")
                    appendLine("Namuna 1: Collocation: \"acknowledge sth\" (xatoni tan olmoq)")
                    appendLine("-> {\"english\": \"The company director immediately acknowledged the critical mistake and apologized to the clients.\", \"uzbek\": \"Kompaniya rahbari jiddiy xatoni darhol tan oldi va mijozlardan uzr so'radi.\"}")
                    appendLine("Namuna 2: Collocation: \"break a promise\" (va'dasini buzmoq)")
                    appendLine("-> {\"english\": \"He has never broken a promise to his family throughout his entire life.\", \"uzbek\": \"U butun umri davomida oilasiga bergan va'dasini aslo buzmagan.\"}")
                }
                "noun" -> {
                    appendLine("VAZIFA: OT (NOUN) BO'LIMIDAGI COLLOCATION UCHUN MISOL GAP VA MUKAMMAL TARJIMA.")
                    appendLine("Bosh so'z (Ot): \"$noun\"")
                    if (verb.isNotBlank()) appendLine("Bog'liq fe'l: \"$verb\"")
                    appendLine("Collocation iborasi: \"$collocation\"")
                    if (meaning.isNotBlank()) appendLine("Asl ma'nosi: \"$meaning\"")
                    appendLine()
                    appendLine("QAT'IY TALABLAR:")
                    appendLine("1. SO'Z TARTIBI VA BOG'LANISH:")
                    appendLine("   - Iborani gapga shunchaki qotib qolgan holda ko'chirma!")
                    appendLine("   - Fe'l + Ot birikmasida: fe'lni gapdagi zamonga moslab turla va ot bilan tabiiy sintaktik bog'la.")
                    appendLine("   - Ot + Fe'l birikmasida (ot ega vazifasida): otni gap boshiga ega qilib qo'y va fe'lni mos zamonda ishlat.")
                    appendLine("   - Birikmadagi \"sth\" yoki \"sb\" belgilarini gapda aslo qoldirma, real ob'ekt yoki shaxs bilan almashtir.")
                    appendLine("   - Otning artikllari (a/an/the) va birlik/ko'plik shakli tabiiy inglizcha me'yorlarga mos bo'lsin.")
                    appendLine("2. YUQORI DARAJADAGI O'ZBEKCHA TARJIMA:")
                    appendLine("   - So'zma-so'z mexanik tarjima qilma!")
                    appendLine("   - O'zbek tili sintaksisiga qat'iy amal qil: KESIM (FE'L) GAPNING ENG OXIRIDA bo'lsin.")
                    appendLine("   - Otning gapdagi roli (bosh kelishik/ega, tushum kelishigi \"-ni\", qaratqich \"-ning\") va iboraning asl ma'nosini chuqur hisobga olib, ravon adabiy o'zbekcha tarjima yoz.")
                    appendLine()
                    appendLine("IDEAL NAMUNALAR (FEW-SHOT):")
                    appendLine("Namuna 1: Collocation: \"reach a decision\" (qarorga kelmoq / qaror qabul qilmoq)")
                    appendLine("-> {\"english\": \"After several hours of intense debate, the committee finally reached a unanimous decision.\", \"uzbek\": \"Bir necha soatlik qizg'in bahs-munozaralardan so'ng, qo'mita a'zolari nihoyat yakdil qarorga kelishdi.\"}")
                    appendLine("Namuna 2: Collocation: \"rumour spread\" (mish-mish tarqalmoq)")
                    appendLine("-> {\"english\": \"The rumour about the company's merger spread rapidly across social media.\", \"uzbek\": \"Kompaniyalarning birlashishi haqidagi mish-mish ijtimoiy tarmoqlarda shiddat bilan tarqaldi.\"}")
                }
                "adjective" -> {
                    appendLine("VAZIFA: SIFAT (ADJECTIVE) BO'LIMIDAGI COLLOCATION UCHUN MISOL GAP VA MUKAMMAL TARJIMA.")
                    appendLine("Asosiy sifat: \"$adjective\"")
                    if (noun.isNotBlank()) appendLine("Ta'riflanayotgan ot: \"$noun\"")
                    appendLine("Collocation iborasi: \"$collocation\"")
                    if (meaning.isNotBlank()) appendLine("Asl ma'nosi: \"$meaning\"")
                    appendLine()
                    appendLine("QAT'IY TALABLAR:")
                    appendLine("1. SO'Z TARTIBI VA TAVSIF:")
                    appendLine("   - Iborani lug'atdagi kabi quruq ko'chirib qo'yma!")
                    appendLine("   - Sifat va ot birikmasini jonli, mazmunli gap ichida tabiiy sintaktik o'rnida qo'lla.")
                    appendLine("   - Agar birikma \"capable of sth\" kabi prepozitsiyali bo'lsa, \"sth\" o'rniga aniq ot yoki gerund qo'y.")
                    appendLine("   - Sifat darajalarini va artikllarni to'g'ri qo'llab, haqiqiy nutqiy gap tuz.")
                    appendLine("2. YUQORI DARAJADAGI O'ZBEKCHA TARJIMA:")
                    appendLine("   - Sifatning o'zbekcha ma'nosini so'zma-so'z kalka qilma (masalan: \"heavy rain\" - \"og'ir yomg'ir\" emas, \"kuchli / sharros yomg'ir\"; \"deep trouble\" - \"katta qiyinchilik\").")
                    appendLine("   - O'zbek tili sintaksisiga qat'iy amal qil: KESIM GAPNING ENG OXIRIDA tursin.")
                    appendLine("   - Nutq madaniyatiga mos, silliq, jozibador va tushunarli adabiy tilda yoz.")
                    appendLine()
                    appendLine("IDEAL NAMUNALAR (FEW-SHOT):")
                    appendLine("Namuna 1: Collocation: \"heavy rain\" (kuchli / sharros yomg'ir)")
                    appendLine("-> {\"english\": \"The international football match was postponed due to torrential, heavy rain.\", \"uzbek\": \"Sharros quygan kuchli yomg'ir tufayli xalqaro futbol uchrashuvi qoldirildi.\"}")
                    appendLine("Namuna 2: Collocation: \"vital role\" (hal qiluvchi / muhim rol)")
                    appendLine("-> {\"english\": \"High-quality education plays a vital role in the long-term development of any society.\", \"uzbek\": \"Sifatli ta'lim har qanday jamiyatning uzoq muddatli rivojlanishida hal qiluvchi o'rin tutadi.\"}")
                }
            }

            if (userCustomPrompt.isNotBlank()) {
                appendLine()
                appendLine("FOYDALANUVCHINING MAXSUS PROMPTI:")
                appendLine(userCustomPrompt)
            }

            if (previousExamples.isNotEmpty()) {
                appendLine()
                appendLine("Eslatma: Quyidagi oldingi gaplarni takrorlama: ${previousExamples.takeLast(3).joinToString("; ")}")
            }

            appendLine()
            appendLine("JAVOB FORMATI:")
            appendLine("Faqatgina toza JSON formatida quyidagi kalitlar bilan javob ber:")
            appendLine("{\"english\": \"[Inglizcha to'g'ri tuzilgan misol gap]\", \"uzbek\": \"[O'zbekcha ravon va aniq tarjima]\"}")
        }

        val systemInstruction = """
Sen oliy toifali professional ingliz-o'zbek sinxron tarjimoni va tilshunosisan.
Sening vazifang — ingliz tilidagi collocation iboralari ishtirokida tabiiy gaplar tuzish va ularni o'zbek tiliga eng yuqori adabiy saviyada, sof, ravon va aniq tarjima qilish.

QAT'IY QOIDALAR:
1. SO'ZMA-SO'Z (KALKA) TARJIMA MUTLAQO TAQIQLANADI.
2. O'ZBEK TILI SINTAKSISIGA QAT'IY AMAL QIL: Gapda kesim (fe'l) doimo eng oxirida kelishi shart.
3. INGLIZCHA SO'Z TARTIBINI NUSXALAMA: O'zbek tili nutq madaniyatiga xos, ona tilida so'zlashuvchi inson qanday gapirsa, shunday tabiiy va go'zal uslubda tarjima qil.
4. BIRIKMANING ASL MA'NOSI: Har bir collocation'ning kontekstdagi nozik va idiomatik ma'nosini chuqur ochib ber.
5. ZAMON VA SHAXS UYG'UNLIGI: Inglizcha zamon va shaxs o'zbekcha tarjimada to'liq va aniq aks etsin.
""".trimIndent()

        return generateContent(
            context = context,
            prompt = prompt,
            systemInstruction = systemInstruction,
            temperatureOverride = getTemperature(context)
        )
    }
}
