package com.example.data.parser

import com.example.data.model.CollocationItem
import com.example.data.model.NounGroup
import org.json.JSONArray
import org.json.JSONObject

object NounJsonParser {

    fun parse(rawJson: String): List<NounGroup> {
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

    private fun parseJsonObject(root: JSONObject): List<NounGroup> {
        // 1. Check if the root object itself is a single noun entry
        // e.g. { "noun": "decision", "verbs": ["make", "take", "reach"] }
        val rootHeadword = root.findHeadword()
        if (rootHeadword.isNotBlank()) {
            val items = extractAllItemsFromObject(rootHeadword, root)
            if (items.isNotEmpty()) {
                return listOf(NounGroup(noun = rootHeadword, items = items))
            }
        }

        // 2. Check for wrapper keys if root has wrapper format
        // e.g. { "nouns": [...] } or { "data": { ... } } or { "entries": [...] }
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

        // 3. Root is a dictionary map: noun -> collocations / verbs
        // e.g. { "decision": ["make", "take", "reach"], "effort": [...] }
        return parseMapObject(root)
    }

    private fun findWrapperKey(root: JSONObject): String? {
        val knownWrappers = listOf(
            "nouns", "noun_collocations", "nouncollocations", "collocations", "collocation",
            "words", "vocabulary", "dictionary", "entries", "entry", "data", "items", "list",
            "results", "result", "records", "table", "content", "response", "payload",
            "verbs", "verb_collocations"
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

    private fun parseMapObject(obj: JSONObject): List<NounGroup> {
        val result = mutableListOf<NounGroup>()
        val keys = obj.keys()

        while (keys.hasNext()) {
            val nounKey = keys.next()
            val value = obj.get(nounKey)
            val items = when (value) {
                is JSONArray -> parseItemsArray(nounKey, value)
                is JSONObject -> parseItemsFromNestedObject(nounKey, value)
                is String -> parseItemsFromString(nounKey, value)
                else -> listOf(createItem(nounKey, value.toString()))
            }
            if (items.isNotEmpty() || nounKey.isNotBlank()) {
                result.add(NounGroup(noun = nounKey.trim(), items = items))
            }
        }
        return result
    }

    private fun parseJsonArray(arr: JSONArray): List<NounGroup> {
        val groupedMap = linkedMapOf<String, MutableList<CollocationItem>>()

        for (i in 0 until arr.length()) {
            val element = arr.opt(i) ?: continue
            when (element) {
                is JSONObject -> {
                    val nounName = element.findHeadword()

                    if (nounName.isNotBlank()) {
                        // Check if this object contains a list of verbs / collocations
                        val subItems = extractAllItemsFromObject(nounName, element)
                        if (subItems.isNotEmpty()) {
                            val currentList = groupedMap.getOrPut(nounName.trim()) { mutableListOf() }
                            currentList.addAll(subItems)
                            continue
                        }

                        // Otherwise it might be a single flat collocation entry
                        // e.g. { "noun": "decision", "verb": "make", "meaning": "qaror qilmoq" }
                        val item = parseSingleItem(nounName, element)
                        val currentList = groupedMap.getOrPut(nounName.trim()) { mutableListOf() }
                        currentList.add(item)
                    } else {
                        // Could be single-key map like { "decision": ["make", "take"] }
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
                            // Loop all keys
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
                    // Array tuple: ["decision", "make a decision"] or ["decision", ["make", "reach"]]
                    if (element.length() > 0) {
                        val first = element.opt(0)
                        if (first is String && first.isNotBlank()) {
                            val noun = first.trim()
                            val currentList = groupedMap.getOrPut(noun) { mutableListOf() }
                            if (element.length() > 1) {
                                val second = element.opt(1)
                                when (second) {
                                    is JSONArray -> currentList.addAll(parseItemsArray(noun, second))
                                    is String -> {
                                        val meaning = if (element.length() > 2) element.optString(2).cleanJsonString() else ""
                                        val colItem = parseArrayPair(noun, element)
                                        if (colItem != null) {
                                            currentList.add(colItem)
                                        } else {
                                            currentList.add(createItem(noun, second, meaning = meaning))
                                        }
                                    }
                                    is JSONObject -> currentList.add(parseSingleItem(noun, second))
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

        return groupedMap.map { (noun, items) -> NounGroup(noun = noun, items = items) }
    }

    /**
     * Extracts ALL collocation items from an object, scanning all known collocation categories.
     * Combines verbs, collocations, adjectives, prepositions, etc.
     */
    private fun extractAllItemsFromObject(noun: String, obj: JSONObject): List<CollocationItem> {
        val result = mutableListOf<CollocationItem>()
        val candidateKeys = listOf(
            "verbs", "verb", "verb_collocations", "collocations", "collocation",
            "phrases", "phrase", "items", "examples", "example",
            "adjectives", "adjective", "adj_collocations",
            "prepositions", "preposition", "prep_collocations", "combinations", "expressions",
            "patterns", "usages", "uses", "words", "list", "data", "+ verb", "+ adj", "+ prep",
            "verb + noun", "adj + noun", "noun + noun", "noun + verb"
        )

        val keys = obj.keys().asSequence().toList()

        for (k in keys) {
            val lower = k.lowercase()
            if (candidateKeys.any { it.equals(lower, ignoreCase = true) || lower.contains("verb") || lower.contains("collocation") || lower.contains("phrase") }) {
                val value = obj.get(k)
                if (value is JSONArray) {
                    result.addAll(parseItemsArray(noun, value, category = k))
                } else if (value is JSONObject) {
                    result.addAll(parseItemsFromNestedObject(noun, value))
                }
            }
        }

        // Attach global meaning if present on obj
        val globalMeaning = obj.optStringCaseInsensitive("meaning", "translation", "trans", "definition", "def", "uzbek", "tarjima").cleanJsonString()
        if (globalMeaning.isNotBlank()) {
            return result.map { item ->
                if (item.meaning.isBlank()) item.copy(meaning = globalMeaning) else item
            }
        }

        return result
    }

    private fun parseItemsFromNestedObject(noun: String, obj: JSONObject): List<CollocationItem> {
        // First check if obj has known candidate categories (e.g. verbs, adjectives)
        val extracted = extractAllItemsFromObject(noun, obj)
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

                    if (cleanKey.lowercase() in metaKeys) {
                        continue
                    }

                    // Determine which is English verb/collocation and which is Uzbek meaning
                    val keyContainsNoun = cleanKey.contains(noun, ignoreCase = true)
                    val valContainsNoun = cleanVal.contains(noun, ignoreCase = true)

                    val (eng, uz) = when {
                        keyContainsNoun -> Pair(cleanKey, cleanVal)
                        valContainsNoun -> Pair(cleanVal, cleanKey)
                        cleanKey.split(" ").size >= cleanVal.split(" ").size -> Pair(cleanKey, cleanVal)
                        else -> Pair(cleanKey, cleanVal)
                    }

                    val isPhrase = eng.contains(noun, ignoreCase = true) || eng.contains(" ")
                    val verb = if (isPhrase) extractLeadingVerb(eng, noun) else eng
                    val collocation = if (isPhrase) eng else CollocationItem.formatVerbNounCollocation(eng, noun)

                    items.add(
                        CollocationItem(
                            verb = verb,
                            noun = noun,
                            collocation = collocation,
                            meaning = uz
                        )
                    )
                }
                is JSONObject -> items.add(parseSingleItem(noun, value))
                is JSONArray -> items.addAll(parseItemsArray(noun, value, category = key))
            }
        }
        return items
    }

    private fun parseItemsArray(noun: String, arr: JSONArray, category: String = ""): List<CollocationItem> {
        val list = mutableListOf<CollocationItem>()
        for (i in 0 until arr.length()) {
            val item = arr.opt(i) ?: continue
            when (item) {
                is JSONArray -> {
                    val pairItem = parseArrayPair(noun, item)
                    if (pairItem != null) {
                        list.add(pairItem)
                    }
                }
                is JSONObject -> list.add(parseSingleItem(noun, item))
                is String -> {
                    val rawStr = item.trim()
                    if (rawStr.startsWith("[") && rawStr.endsWith("]")) {
                        try {
                            val innerArr = JSONArray(rawStr)
                            val pairItem = parseArrayPair(noun, innerArr)
                            if (pairItem != null) {
                                list.add(pairItem)
                                continue
                            }
                        } catch (_: Exception) {}
                    }
                    list.add(createItem(noun, rawStr, category = category))
                }
                else -> list.add(createItem(noun, item.toString(), category = category))
            }
        }
        return list
    }

    private fun parseArrayPair(noun: String, arr: JSONArray): CollocationItem? {
        if (arr.length() == 0) return null
        if (arr.length() == 1) {
            return createItem(noun, arr.optString(0))
        }

        val s0 = arr.optString(0).cleanJsonString()
        val s1 = arr.optString(1).cleanJsonString()
        val s2 = if (arr.length() > 2) arr.optString(2).cleanJsonString() else ""

        if (arr.length() >= 3 && s2.isNotBlank()) {
            // [ "make", "make a decision", "qaror qilmoq" ]
            val verb = if (s0.split(" ").size <= s1.split(" ").size) s0 else s1
            val col = if (s1.contains(noun, ignoreCase = true) || s1.split(" ").size > s0.split(" ").size) s1 else s0
            return CollocationItem(
                verb = verb,
                noun = noun,
                collocation = col,
                meaning = s2
            )
        }

        // Two elements: [ "make", "qaror qilmoq" ] or [ "make", "make a decision" ] or [ "make a decision", "qaror qilmoq" ]
        val posTags = setOf("verb", "adj", "adjective", "noun", "prep", "preposition", "v", "a", "n", "p", "phr")
        val (col, meaning) = when {
            posTags.contains(s0.lowercase()) -> Pair(s1, s0)
            posTags.contains(s1.lowercase()) -> Pair(s0, s1)
            s1.contains(noun, ignoreCase = true) && !s0.contains(noun, ignoreCase = true) -> Pair(s1, s0)
            s0.contains(noun, ignoreCase = true) && !s1.contains(noun, ignoreCase = true) -> Pair(s0, s1)
            s1.split(" ").size > s0.split(" ").size -> Pair(s1, s0)
            s0.split(" ").size > s1.split(" ").size -> Pair(s0, s1)
            else -> Pair(s0, s1)
        }

        val verb = extractLeadingVerb(col, noun).ifBlank {
            if (!col.contains(noun, ignoreCase = true) && col.split(" ").size <= 2) col else ""
        }
        val finalCollocation = if (col.contains(noun, ignoreCase = true)) {
            col
        } else if (verb.isNotBlank()) {
            CollocationItem.formatVerbNounCollocation(verb, noun)
        } else {
            col.ifBlank { noun }
        }

        return CollocationItem(
            verb = verb,
            noun = noun,
            collocation = finalCollocation,
            meaning = if (meaning == col) "" else meaning
        )
    }

    private fun parseSingleItem(noun: String, obj: JSONObject): CollocationItem {
        val verb = obj.optStringCaseInsensitive("verb", "v", "action", "head_verb").cleanJsonString()
        val collocation = obj.optStringCaseInsensitive(
            "collocation", "phrase", "text", "expression", "name", "title", "val", "value"
        ).cleanJsonString()

        val meaning = obj.optStringCaseInsensitive(
            "meaning", "translation", "trans", "definition", "def", "uzbek", "uz", "tarjima", "meaning_uz"
        ).cleanJsonString()

        val example = obj.optStringCaseInsensitive("example", "sample", "usage", "sentence").cleanJsonString()
        val objNoun = obj.optStringCaseInsensitive("noun", "n", "word").cleanJsonString()
        val effectiveNoun = objNoun.ifBlank { noun }

        val resolvedVerb = when {
            verb.isNotBlank() -> verb
            collocation.isNotBlank() -> extractLeadingVerb(collocation, effectiveNoun)
            else -> ""
        }

        val resolvedCollocation = when {
            collocation.isNotBlank() && !collocation.equals(effectiveNoun, ignoreCase = true) -> collocation
            resolvedVerb.isNotBlank() -> CollocationItem.formatVerbNounCollocation(resolvedVerb, effectiveNoun)
            collocation.isNotBlank() -> collocation
            else -> effectiveNoun
        }

        return CollocationItem(
            verb = resolvedVerb,
            noun = effectiveNoun,
            collocation = resolvedCollocation,
            meaning = meaning,
            example = example
        )
    }

    private fun createItem(noun: String, text: String, category: String = "", meaning: String = ""): CollocationItem {
        val cleaned = text.cleanJsonString()
        if (cleaned.isBlank()) return CollocationItem(noun = noun, meaning = meaning)

        // 1. If text already contains the noun, it is already a full collocation phrase
        if (cleaned.contains(noun, ignoreCase = true)) {
            val verb = extractLeadingVerb(cleaned, noun)
            return CollocationItem(
                verb = verb,
                noun = noun,
                collocation = cleaned,
                meaning = meaning
            )
        }

        // 2. If categorized as adjective
        if (category.contains("adj", ignoreCase = true)) {
            return CollocationItem(
                noun = noun,
                collocation = "$cleaned $noun",
                meaning = meaning
            )
        }

        // 3. If categorized as preposition
        if (category.contains("prep", ignoreCase = true)) {
            return CollocationItem(
                noun = noun,
                collocation = "$noun $cleaned",
                meaning = meaning
            )
        }

        // 4. Default: Treated as a verb related to this noun!
        // E.g. cleaned = "make", noun = "decision" -> phrase = "make a decision"
        val phrase = CollocationItem.formatVerbNounCollocation(cleaned, noun)
        return CollocationItem(
            verb = cleaned,
            noun = noun,
            collocation = phrase,
            meaning = meaning
        )
    }

    private fun parseItemsFromString(noun: String, raw: String): List<CollocationItem> {
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
                .map { createItem(noun, it) }
        }
        return listOf(createItem(noun, cleaned))
    }

    private fun parseFallbackLines(raw: String): List<NounGroup> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<NounGroup>()
        for (line in lines) {
            if (line.startsWith("{") && line.endsWith("}")) {
                try {
                    val obj = JSONObject(line)
                    val headword = obj.findHeadword()
                    if (headword.isNotBlank()) {
                        val items = extractAllItemsFromObject(headword, obj)
                        result.add(NounGroup(noun = headword, items = items))
                    }
                } catch (_: Exception) {}
            }
        }
        return result
    }

    private fun extractLeadingVerb(phrase: String, noun: String): String {
        val tokens = phrase.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.isNotEmpty()) {
            val first = tokens.first().trim()
            if (!first.equals(noun, ignoreCase = true)) {
                if (tokens.size >= 2 && listOf("up", "to", "out", "in", "on", "with", "into", "at", "for", "from").contains(tokens[1].lowercase())) {
                    return "${tokens[0]} ${tokens[1]}"
                }
                return first
            } else if (tokens.size >= 2) {
                // If noun is first word, verb is the second word! E.g. "report acknowledge sth", "rumour spread"
                val second = tokens[1].trim()
                if (!listOf("a", "an", "the", "of", "in", "on", "at", "to", "for", "with").contains(second.lowercase())) {
                    if (tokens.size >= 3 && listOf("up", "to", "out", "in", "on", "with", "into", "at", "for", "from").contains(tokens[2].lowercase())) {
                        return "${tokens[1]} ${tokens[2]}"
                    }
                    return second
                }
            }
        }
        return ""
    }

    private fun JSONObject.findHeadword(): String {
        return optStringCaseInsensitive(
            "noun", "word", "headword", "lemma", "term", "title", "entry", "name", "id", "key"
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
