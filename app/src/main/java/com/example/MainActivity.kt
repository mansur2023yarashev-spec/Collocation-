package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.parser.AdjectiveJsonParser
import com.example.data.parser.NounJsonParser
import com.example.data.parser.VerbJsonParser
import com.example.ui.screens.AdjectiveCollocationsScreen
import com.example.ui.screens.CollocationsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NounCollocationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.TextToSpeechHelper

enum class AppNavScreen {
    HOME,
    COLLOCATIONS,
    NOUN_COLLOCATIONS,
    ADJECTIVE_COLLOCATIONS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ttsHelper = TextToSpeechHelper(this)

        setContent {
            CollocationsMainApp(ttsHelper = ttsHelper)
        }
    }

    override fun onDestroy() {
        ttsHelper?.shutdown()
        ttsHelper = null
        super.onDestroy()
    }
}

@Composable
fun CollocationsMainApp(ttsHelper: TextToSpeechHelper?) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("collocations_app_prefs", Context.MODE_PRIVATE) }

    var isDarkTheme by rememberSaveable {
        mutableStateOf(prefs.getBoolean("is_dark_theme", true))
    }

    fun toggleDarkTheme() {
        val newTheme = !isDarkTheme
        isDarkTheme = newTheme
        prefs.edit().putBoolean("is_dark_theme", newTheme).apply()
    }

    var currentScreen by rememberSaveable { mutableStateOf(AppNavScreen.HOME) }
    var selectedVerbName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedNounName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAdjectiveName by rememberSaveable { mutableStateOf<String?>(null) }

    val homeListState = rememberLazyListState()
    val homeNounListState = rememberLazyListState()
    val homeAdjectiveListState = rememberLazyListState()
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    var homeSelectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Verb JSON state
    var rawJson by remember {
        mutableStateOf(prefs.getString("saved_raw_json", "") ?: "")
    }

    fun handleJsonUpdated(newJson: String) {
        rawJson = newJson
        prefs.edit().putString("saved_raw_json", newJson).apply()
    }

    // Noun JSON state
    var rawNounJson by remember {
        mutableStateOf(prefs.getString("saved_noun_raw_json", "") ?: "")
    }

    fun handleNounJsonUpdated(newJson: String) {
        rawNounJson = newJson
        prefs.edit().putString("saved_noun_raw_json", newJson).apply()
    }

    // Adjective JSON state
    var rawAdjectiveJson by remember {
        mutableStateOf(prefs.getString("saved_adjective_raw_json", "") ?: "")
    }

    fun handleAdjectiveJsonUpdated(newJson: String) {
        rawAdjectiveJson = newJson
        prefs.edit().putString("saved_adjective_raw_json", newJson).apply()
    }

    // Verb Favorites
    var favorites by remember {
        val savedFavs = prefs.getStringSet("saved_favorites", emptySet()) ?: emptySet()
        mutableStateOf(savedFavs)
    }

    fun toggleFavorite(verb: String) {
        val lower = verb.lowercase()
        val updated = if (favorites.contains(lower)) {
            favorites - lower
        } else {
            favorites + lower
        }
        favorites = updated
        prefs.edit().putStringSet("saved_favorites", updated).apply()
    }

    // Noun Favorites
    var nounFavorites by remember {
        val savedFavs = prefs.getStringSet("saved_noun_favorites", emptySet()) ?: emptySet()
        mutableStateOf(savedFavs)
    }

    fun toggleNounFavorite(noun: String) {
        val lower = noun.lowercase()
        val updated = if (nounFavorites.contains(lower)) {
            nounFavorites - lower
        } else {
            nounFavorites + lower
        }
        nounFavorites = updated
        prefs.edit().putStringSet("saved_noun_favorites", updated).apply()
    }

    // Adjective Favorites
    var adjectiveFavorites by remember {
        val savedFavs = prefs.getStringSet("saved_adjective_favorites", emptySet()) ?: emptySet()
        mutableStateOf(savedFavs)
    }

    fun toggleAdjectiveFavorite(adjective: String) {
        val lower = adjective.lowercase()
        val updated = if (adjectiveFavorites.contains(lower)) {
            adjectiveFavorites - lower
        } else {
            adjectiveFavorites + lower
        }
        adjectiveFavorites = updated
        prefs.edit().putStringSet("saved_adjective_favorites", updated).apply()
    }

    val verbGroups by remember(rawJson) {
        derivedStateOf {
            VerbJsonParser.parse(rawJson)
        }
    }

    val nounGroups by remember(rawNounJson) {
        derivedStateOf {
            NounJsonParser.parse(rawNounJson)
        }
    }

    val adjectiveGroups by remember(rawAdjectiveJson) {
        derivedStateOf {
            AdjectiveJsonParser.parse(rawAdjectiveJson)
        }
    }

    val selectedVerbGroup = remember(verbGroups, selectedVerbName) {
        verbGroups.firstOrNull { it.verb.equals(selectedVerbName, ignoreCase = true) }
    }

    val selectedNounGroup = remember(nounGroups, selectedNounName) {
        nounGroups.firstOrNull { it.noun.equals(selectedNounName, ignoreCase = true) }
    }

    val selectedAdjectiveGroup = remember(adjectiveGroups, selectedAdjectiveName) {
        adjectiveGroups.firstOrNull { it.adjective.equals(selectedAdjectiveName, ignoreCase = true) }
    }

    // Hardware back navigation
    BackHandler(enabled = currentScreen != AppNavScreen.HOME) {
        currentScreen = AppNavScreen.HOME
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                AppNavScreen.HOME -> {
                    HomeScreen(
                        verbGroups = verbGroups,
                        favorites = favorites,
                        onToggleFavorite = { verb -> toggleFavorite(verb) },
                        onSelectVerb = { group ->
                            selectedVerbName = group.verb
                            currentScreen = AppNavScreen.COLLOCATIONS
                        },
                        nounGroups = nounGroups,
                        nounFavorites = nounFavorites,
                        onToggleNounFavorite = { noun -> toggleNounFavorite(noun) },
                        onSelectNoun = { group ->
                            selectedNounName = group.noun
                            currentScreen = AppNavScreen.NOUN_COLLOCATIONS
                        },
                        adjectiveGroups = adjectiveGroups,
                        adjectiveFavorites = adjectiveFavorites,
                        onToggleAdjectiveFavorite = { adj -> toggleAdjectiveFavorite(adj) },
                        onSelectAdjective = { group ->
                            selectedAdjectiveName = group.adjective
                            currentScreen = AppNavScreen.ADJECTIVE_COLLOCATIONS
                        },
                        onNavigateToSettings = { currentScreen = AppNavScreen.SETTINGS },
                        onJsonUpdated = { newJson -> handleJsonUpdated(newJson) },
                        onNounJsonUpdated = { newJson -> handleNounJsonUpdated(newJson) },
                        onAdjectiveJsonUpdated = { newJson -> handleAdjectiveJsonUpdated(newJson) },
                        ttsHelper = ttsHelper,
                        listState = homeListState,
                        nounListState = homeNounListState,
                        adjectiveListState = homeAdjectiveListState,
                        searchQuery = homeSearchQuery,
                        onSearchQueryChange = { homeSearchQuery = it },
                        selectedTab = homeSelectedTab,
                        onSelectedTabChange = { homeSelectedTab = it },
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { toggleDarkTheme() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppNavScreen.COLLOCATIONS -> {
                    val group = selectedVerbGroup
                    if (group != null) {
                        CollocationsScreen(
                            verbGroup = group,
                            isFavorite = favorites.contains(group.verb.lowercase()),
                            onToggleFavorite = { toggleFavorite(group.verb) },
                            onNavigateBack = { currentScreen = AppNavScreen.HOME },
                            onNavigateToSettings = { currentScreen = AppNavScreen.SETTINGS },
                            ttsHelper = ttsHelper,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        currentScreen = AppNavScreen.HOME
                    }
                }
                AppNavScreen.NOUN_COLLOCATIONS -> {
                    val group = selectedNounGroup
                    if (group != null) {
                        NounCollocationsScreen(
                            nounGroup = group,
                            isFavorite = nounFavorites.contains(group.noun.lowercase()),
                            onToggleFavorite = { toggleNounFavorite(group.noun) },
                            onNavigateBack = { currentScreen = AppNavScreen.HOME },
                            onNavigateToSettings = { currentScreen = AppNavScreen.SETTINGS },
                            ttsHelper = ttsHelper,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        currentScreen = AppNavScreen.HOME
                    }
                }
                AppNavScreen.ADJECTIVE_COLLOCATIONS -> {
                    val group = selectedAdjectiveGroup
                    if (group != null) {
                        AdjectiveCollocationsScreen(
                            adjectiveGroup = group,
                            isFavorite = adjectiveFavorites.contains(group.adjective.lowercase()),
                            onToggleFavorite = { toggleAdjectiveFavorite(group.adjective) },
                            onNavigateBack = { currentScreen = AppNavScreen.HOME },
                            onNavigateToSettings = { currentScreen = AppNavScreen.SETTINGS },
                            ttsHelper = ttsHelper,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        currentScreen = AppNavScreen.HOME
                    }
                }
                AppNavScreen.SETTINGS -> {
                    SettingsScreen(
                        onNavigateBack = { currentScreen = AppNavScreen.HOME },
                        onJsonUpdated = { newJson -> handleJsonUpdated(newJson) },
                        onNounJsonUpdated = { newJson -> handleNounJsonUpdated(newJson) },
                        onAdjectiveJsonUpdated = { newJson -> handleAdjectiveJsonUpdated(newJson) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
