package com.example.data.parser

import com.example.data.model.AdjectiveGroup
import com.example.data.model.CollocationItem
import org.json.JSONArray
import org.json.JSONObject

object AdjectiveJsonParser {

    fun parse(rawJson: String): List<AdjectiveGroup> {
        val trimmed = rawJson.trim().removePrefix("\uFEFF").trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            when {
                trimmed.startsWith("{") -> parseJsonObject(JSONObject(trimmed))
                trimmed.startsWith("[") -> parseJsonArray(JSONArray(trimmed))
                else -> parseFallbackLines(trimmed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            parseFallbackLines(trimmed)
        }
    }

    private fun parseJsonObject(root: JSONObject): List<AdjectiveGroup> {
        // 1. Check if root is a single adjective entry
        val rootHeadword = root.findHeadword()
        if (rootHeadword.isNotBlank()) {
            val items = extractAllItemsFromObject(rootHeadword, root)
            if (items.isNotEmpty()) {
                return listOf(AdjectiveGroup(adjective = rootHeadword, items = items))
            }
        }

        // 2. Check for wrapper keys
        val wrapperKey = findWrapperKey(root)
        if (wrapperKey != null) {
            val value = root.get(wrapperKey)
            if (value is JSONArray) {
                val parsed = parseJsonArray(value)
                if (parsed.isNotEmpty()) return parsed
            } else if (value is JSONObject) {
                val parsed = parseJsonObject(value)
                if (parsed.isNotEmpty()) return parsed
            }
        }

        // 3. Root is a dictionary map: adjective -> collocations / nouns
        return parseMapObject(root)
    }

    private fun findWrapperKey(root: JSONObject): String? {
        val knownWrappers = listOf(
            "adjectives", "adjective_collocations", "adjectivecollocations", "adj",
            "collocations", "collocation", "words", "vocabulary", "dictionary",
            "entries", "entry", "data", "items", "list", "results", "result",
            "records", "table", "content", "response", "payload"
        )

        val keys = root.keys().asSequence().toList()
        if (keys.isEmpty()) return null

        for (w in knownWrappers) {
            for (k in keys) {
                if (k.equals(w, ignoreCase = true)) {
                    val v = root.opt(k)
                    if (v is JSONArray || v is JSONObject) {
                        return k
                    }
                }
            }
        }
        return null
    }

    private fun parseMapObject(obj: JSONObject): List<AdjectiveGroup> {
        val result = mutableListOf<AdjectiveGroup>()
        val keys = obj.keys()

        while (keys.hasNext()) {
            val adjKey = keys.next()
            val value = obj.get(adjKey)
            val items = when (value) {
                is JSONArray -> parseItemsArray(adjKey, value)
                is JSONObject -> parseItemsFromNestedObject(adjKey, value)
                is String -> parseItemsFromString(adjKey, value)
                else -> listOf(createItem(adjKey, value.toString()))
            }
            if (items.isNotEmpty() || adjKey.isNotBlank()) {
                result.add(AdjectiveGroup(adjective = adjKey.trim(), items = items))
            }
        }
        return result
    }

    private fun parseJsonArray(arr: JSONArray): List<AdjectiveGroup> {
        val groupedMap = linkedMapOf<String, MutableList<CollocationItem>>()

        for (i in 0 until arr.length()) {
            val element = arr.opt(i) ?: continue
            when (element) {
                is JSONObject -> {
                    val adjName = element.findHeadword()

                    if (adjName.isNotBlank()) {
                        val subItems = extractAllItemsFromObject(adjName, element)
                        if (subItems.isNotEmpty()) {
                            val currentList = groupedMap.getOrPut(adjName.trim()) { mutableListOf() }
                            currentList.addAll(subItems)
                            continue
                        }

                        val item = parseSingleItem(adjName, element)
                        val currentList = groupedMap.getOrPut(adjName.trim()) { mutableListOf() }
                        currentList.add(item)
                    } else {
                        val keys = element.keys().asSequence().toList()
                        if (keys.size == 1) {
                            val k = keys[0]
                            val v = element.get(k)
                            val items = when (v) {
                                is JSONArray -> parseItemsArray(k, v)
                                is JSONObject -> parseItemsFromNestedObject(k, v)
                                is String -> parseItemsFromString(k, v)
                                else -> emptyList()
                            }
                            val currentList = groupedMap.getOrPut(k.trim()) { mutableListOf() }
                            currentList.addAll(items)
                        } else {
                            for (k in keys) {
                                val v = element.get(k)
                                if (v is JSONArray) {
                                    val items = parseItemsArray(k, v)
                                    val currentList = groupedMap.getOrPut(k.trim()) { mutableListOf() }
                                    currentList.addAll(items)
                                }
                            }
                        }
                    }
                }

                is JSONArray -> {
                    if (element.length() > 0) {
                        val first = element.opt(0)
                        if (first is String && first.isNotBlank()) {
                            val adj = first.trim()
                            val currentList = groupedMap.getOrPut(adj) { mutableListOf() }
                            if (element.length() > 1) {
                                val second = element.opt(1)
                                when (second) {
                                    is JSONArray -> currentList.addAll(parseItemsArray(adj, second))
                                    is String -> {
                                        val meaning = if (element.length() > 2) element.optString(2).cleanJsonString() else ""
                                        val colItem = parseArrayPair(adj, element)
                                        if (colItem != null) {
                                            currentList.add(colItem)
                                        } else {
                                            currentList.add(createItem(adj, second, meaning = meaning))
                                        }
                                    }
                                    is JSONObject -> currentList.add(parseSingleItem(adj, second))
                                }
                            }
                        }
                    }
                }

                is String -> {
                    if (element.isNotBlank()) {
                        groupedMap.getOrPut(element.trim()) { mutableListOf() }
                    }
                }
            }
        }

        return groupedMap.map { (adj, items) -> AdjectiveGroup(adjective = adj, items = items) }
    }

    private fun extractAllItemsFromObject(adj: String, obj: JSONObject): List<CollocationItem> {
        val result = mutableListOf<CollocationItem>()
        val candidateKeys = listOf(
            "nouns", "noun", "noun_collocations", "collocations", "collocation",
            "phrases", "phrase", "items", "examples", "example",
            "combinations", "expressions", "patterns", "words", "list", "data"
        )

        val keys = obj.keys().asSequence().toList()

        for (k in keys) {
            val lower = k.lowercase()
            if (candidateKeys.any { it.equals(lower, ignoreCase = true) || lower.contains("noun") || lower.contains("collocation") || lower.contains("phrase") }) {
                val value = obj.get(k)
                if (value is JSONArray) {
                    result.addAll(parseItemsArray(adj, value))
                } else if (value is JSONObject) {
                    result.addAll(parseItemsFromNestedObject(adj, value))
                }
            }
        }

        val globalMeaning = obj.optStringCaseInsensitive("meaning", "translation", "trans", "definition", "def", "uzbek", "tarjima").cleanJsonString()
        if (globalMeaning.isNotBlank()) {
            return result.map { item ->
                if (item.meaning.isBlank()) item.copy(meaning = globalMeaning) else item
            }
        }

        return result
    }

    private fun parseItemsFromNestedObject(adj: String, obj: JSONObject): List<CollocationItem> {
        val extracted = extractAllItemsFromObject(adj, obj)
        if (extracted.isNotEmpty()) return extracted

        val items = mutableListOf<CollocationItem>()
        val keys = obj.keys()
        val metaKeys = setOf("meaning", "definition", "translation", "trans", "uzbek", "audio", "pos", "type")

        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.opt(key)

            when (value) {
                is String -> {
                    val cleanKey = key.cleanJsonString()
                    val cleanVal = value.cleanJsonString()

                    if (cleanKey.lowercase() in metaKeys) continue

                    val (eng, uz) = if (cleanKey.contains(adj, ignoreCase = true) || cleanKey.split(" ").size >= cleanVal.split(" ").size) {
                        Pair(cleanKey, cleanVal)
                    } else {
                        Pair(cleanVal, cleanKey)
                    }

                    val noun = extractNounFromPhrase(eng, adj).ifBlank {
                        if (!eng.contains(adj, ignoreCase = true)) eng else ""
                    }
                    val collocation = if (eng.contains(adj, ignoreCase = true)) eng else CollocationItem.formatAdjectiveNounCollocation(adj, eng)

                    items.add(
                        CollocationItem(
                            adjective = adj,
                            noun = noun,
                            collocation = collocation,
                            meaning = uz
                        )
                    )
                }
                is JSONObject -> items.add(parseSingleItem(adj, value))
                is JSONArray -> items.addAll(parseItemsArray(adj, value))
            }
        }
        return items
    }

    private fun parseItemsArray(adj: String, arr: JSONArray): List<CollocationItem> {
        val list = mutableListOf<CollocationItem>()
        for (i in 0 until arr.length()) {
            val item = arr.opt(i) ?: continue
            when (item) {
                is JSONArray -> {
                    val pairItem = parseArrayPair(adj, item)
                    if (pairItem != null) {
                        list.add(pairItem)
                    }
                }
                is JSONObject -> list.add(parseSingleItem(adj, item))
                is String -> {
                    val rawStr = item.trim()
                    if (rawStr.startsWith("[") && rawStr.endsWith("]")) {
                        try {
                            val innerArr = JSONArray(rawStr)
                            val pairItem = parseArrayPair(adj, innerArr)
                            if (pairItem != null) {
                                list.add(pairItem)
                                continue
                            }
                        } catch (_: Exception) {}
                    }
                    list.add(createItem(adj, rawStr))
                }
                else -> list.add(createItem(adj, item.toString()))
            }
        }
        return list
    }

    private fun parseArrayPair(adj: String, arr: JSONArray): CollocationItem? {
        if (arr.length() == 0) return null
        if (arr.length() == 1) {
            return createItem(adj, arr.optString(0))
        }

        val s0 = arr.optString(0).cleanJsonString()
        val s1 = arr.optString(1).cleanJsonString()
        val s2 = if (arr.length() > 2) arr.optString(2).cleanJsonString() else ""

        if (arr.length() >= 3 && s2.isNotBlank()) {
            // [ "tea", "strong tea", "achchiq choy" ]
            val noun = if (s0.split(" ").size <= s1.split(" ").size) s0 else s1
            val col = if (s1.contains(adj, ignoreCase = true) || s1.split(" ").size > s0.split(" ").size) s1 else s0
            return CollocationItem(
                adjective = adj,
                noun = noun,
                collocation = col,
                meaning = s2
            )
        }

        // Two elements: [ "tea", "strong tea" ] or [ "tea", "achchiq choy" ] or [ "strong tea", "achchiq choy" ]
        val (col, meaning) = when {
            s1.contains(adj, ignoreCase = true) && !s0.contains(adj, ignoreCase = true) -> Pair(s1, s0)
            s0.contains(adj, ignoreCase = true) && !s1.contains(adj, ignoreCase = true) -> Pair(s0, s1)
            s1.split(" ").size > s0.split(" ").size -> Pair(s1, s0)
            s0.split(" ").size > s1.split(" ").size -> Pair(s0, s1)
            else -> Pair(s0, s1)
        }

        val noun = extractNounFromPhrase(col, adj).ifBlank {
            if (!col.contains(adj, ignoreCase = true) && col.split(" ").size <= 2) col else ""
        }
        val finalCollocation = if (col.contains(adj, ignoreCase = true)) {
            col
        } else if (noun.isNotBlank()) {
            CollocationItem.formatAdjectiveNounCollocation(adj, noun)
        } else {
            col.ifBlank { adj }
        }

        return CollocationItem(
            adjective = adj,
            noun = noun,
            collocation = finalCollocation,
            meaning = if (meaning == col) "" else meaning
        )
    }

    private fun parseSingleItem(adj: String, obj: JSONObject): CollocationItem {
        val noun = obj.optStringCaseInsensitive("noun", "n", "word").cleanJsonString()
        val collocation = obj.optStringCaseInsensitive(
            "collocation", "phrase", "text", "expression", "name", "title", "val", "value"
        ).cleanJsonString()

        val meaning = obj.optStringCaseInsensitive(
            "meaning", "translation", "trans", "definition", "def", "uzbek", "uz", "tarjima", "meaning_uz"
        ).cleanJsonString()

        val example = obj.optStringCaseInsensitive("example", "sample", "usage", "sentence").cleanJsonString()
        val objAdj = obj.optStringCaseInsensitive("adjective", "adj", "a").cleanJsonString()
        val effectiveAdj = objAdj.ifBlank { adj }

        val resolvedNoun = when {
            noun.isNotBlank() -> noun
            collocation.isNotBlank() -> extractNounFromPhrase(collocation, effectiveAdj)
            else -> ""
        }

        val resolvedCollocation = when {
            collocation.isNotBlank() && !collocation.equals(effectiveAdj, ignoreCase = true) -> collocation
            resolvedNoun.isNotBlank() -> CollocationItem.formatAdjectiveNounCollocation(effectiveAdj, resolvedNoun)
            collocation.isNotBlank() -> collocation
            else -> effectiveAdj
        }

        return CollocationItem(
            adjective = effectiveAdj,
            noun = resolvedNoun,
            collocation = resolvedCollocation,
            meaning = meaning,
            example = example
        )
    }

    private fun createItem(adj: String, text: String, meaning: String = ""): CollocationItem {
        val cleaned = text.cleanJsonString()
        if (cleaned.isBlank()) return CollocationItem(adjective = adj, meaning = meaning)

        // 1. If text already contains the adjective, it is already a full collocation phrase
        if (cleaned.contains(adj, ignoreCase = true)) {
            val noun = extractNounFromPhrase(cleaned, adj)
            return CollocationItem(
                adjective = adj,
                noun = noun,
                collocation = cleaned,
                meaning = meaning
            )
        }

        // 2. Otherwise text is the noun that pairs with this adjective
        val phrase = CollocationItem.formatAdjectiveNounCollocation(adj, cleaned)
        return CollocationItem(
            adjective = adj,
            noun = cleaned,
            collocation = phrase,
            meaning = meaning
        )
    }

    private fun parseItemsFromString(adj: String, raw: String): List<CollocationItem> {
        val cleaned = raw.cleanJsonString()
        if (cleaned.contains("\n") || cleaned.contains(",") || cleaned.contains(";")) {
            val delimiter = when {
                cleaned.contains("\n") -> "\n"
                cleaned.contains(";") -> ";"
                else -> ","
            }
            return cleaned.split(delimiter)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { createItem(adj, it) }
        }
        return listOf(createItem(adj, cleaned))
    }

    private fun parseFallbackLines(raw: String): List<AdjectiveGroup> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<AdjectiveGroup>()
        for (line in lines) {
            if (line.startsWith("{") && line.endsWith("}")) {
                try {
                    val obj = JSONObject(line)
                    val headword = obj.findHeadword()
                    if (headword.isNotBlank()) {
                        val items = extractAllItemsFromObject(headword, obj)
                        result.add(AdjectiveGroup(adjective = headword, items = items))
                    }
                } catch (_: Exception) {}
            }
        }
        return result
    }

    /**
     * Extracts the noun from a phrase containing the adjective.
     * e.g., "strong tea" with adj "strong" -> "tea"
     *       "heavy traffic" with adj "heavy" -> "traffic"
     *       "a deep breath" with adj "deep" -> "breath"
     */
    private fun extractNounFromPhrase(phrase: String, adj: String): String {
        val trimmed = phrase.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        if (words.size == 1) return if (!words[0].equals(adj, ignoreCase = true)) words[0] else ""

        val adjLower = adj.lowercase().trim()
        val stopWords = setOf(
            "a", "an", "the", "to", "of", "in", "for", "on", "with",
            "at", "by", "from", "up", "about", "into", "over", "after",
            "sth", "sb", "very", "quite", "extremely", "really"
        )

        val candidates = words.filter { word ->
            val clean = word.lowercase().filter { it.isLetter() }
            clean != adjLower && !clean.startsWith(adjLower) && clean !in stopWords
        }

        return candidates.lastOrNull()?.filter { it.isLetter() } ?: ""
    }

    private fun JSONObject.findHeadword(): String {
        return optStringCaseInsensitive(
            "adjective", "adj", "word", "headword", "lemma", "term", "title", "entry", "name", "id", "key"
        ).cleanJsonString()
    }

    private fun JSONObject.optStringCaseInsensitive(vararg keys: String): String {
        for (candidate in keys) {
            val it = this.keys()
            while (it.hasNext()) {
                val realKey = it.next()
                if (realKey.equals(candidate, ignoreCase = true)) {
                    val value = this.opt(realKey)
                    if (value is String && value.isNotBlank()) {
                        return value
                    } else if (value != null && value !is JSONObject && value !is JSONArray) {
                        return value.toString()
                    }
                }
            }
        }
        return ""
    }

    private fun String.cleanJsonString(): String {
        return this.removePrefix("[").removeSuffix("]").replace("\"", "").trim()
    }
}
