package com.example.data.parser

import com.example.data.model.CollocationItem
import com.example.data.model.VerbGroup
import org.json.JSONArray
import org.json.JSONObject

object VerbJsonParser {

    fun parse(rawJson: String): List<VerbGroup> {
        val trimmed = rawJson.trim().removePrefix("\uFEFF").trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            when {
                trimmed.startsWith("{") -> parseJsonObject(JSONObject(trimmed))
                trimmed.startsWith("[") -> parseJsonArray(JSONArray(trimmed))
                else -> emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseJsonObject(root: JSONObject): List<VerbGroup> {
        val wrapperKeys = listOf("verbs", "collocations", "data", "items", "list")
        for (key in wrapperKeys) {
            if (root.has(key)) {
                val value = root.get(key)
                if (value is JSONArray) {
                    return parseJsonArray(value)
                } else if (value is JSONObject) {
                    return parseMapObject(value)
                }
            }
        }
        return parseMapObject(root)
    }

    private fun parseMapObject(obj: JSONObject): List<VerbGroup> {
        val result = mutableListOf<VerbGroup>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val verbKey = keys.next()
            val value = obj.get(verbKey)
            val items = when (value) {
                is JSONArray -> parseItemsArray(verbKey, value)
                is JSONObject -> parseItemsFromNestedObject(verbKey, value)
                is String -> listOf(createItem(verbKey, value))
                else -> emptyList()
            }
            if (items.isNotEmpty() || verbKey.isNotBlank()) {
                result.add(VerbGroup(verb = verbKey.trim(), items = items))
            }
        }
        return result
    }

    private fun parseJsonArray(arr: JSONArray): List<VerbGroup> {
        val groupedMap = linkedMapOf<String, MutableList<CollocationItem>>()

        for (i in 0 until arr.length()) {
            val element = arr.opt(i) ?: continue
            if (element is JSONObject) {
                val verbName = element.optString("verb").ifBlank {
                    element.optString("word").ifBlank {
                        element.optString("name")
                    }
                }

                if (verbName.isNotBlank()) {
                    val subItems = extractListFromObject(verbName, element)
                    if (subItems.isNotEmpty()) {
                        val currentList = groupedMap.getOrPut(verbName.trim()) { mutableListOf() }
                        currentList.addAll(subItems)
                        continue
                    }
                    val item = parseSingleItem(verbName, element)
                    val currentList = groupedMap.getOrPut(verbName.trim()) { mutableListOf() }
                    currentList.add(item)
                } else {
                    val keys = element.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = element.get(k)
                        if (v is JSONArray) {
                            val items = parseItemsArray(k, v)
                            val currentList = groupedMap.getOrPut(k.trim()) { mutableListOf() }
                            currentList.addAll(items)
                        }
                    }
                }
            } else if (element is String && element.isNotBlank()) {
                groupedMap.getOrPut(element.trim()) { mutableListOf() }
            }
        }

        return groupedMap.map { (verb, items) -> VerbGroup(verb = verb, items = items) }
    }

    private fun extractListFromObject(verb: String, obj: JSONObject): List<CollocationItem> {
        val candidateKeys = listOf("collocations", "nouns", "items", "phrases", "examples", "words")
        for (key in candidateKeys) {
            if (obj.has(key)) {
                val value = obj.get(key)
                if (value is JSONArray) {
                    return parseItemsArray(verb, value)
                }
            }
        }
        return emptyList()
    }

    private fun parseItemsFromNestedObject(verb: String, obj: JSONObject): List<CollocationItem> {
        val extracted = extractListFromObject(verb, obj)
        if (extracted.isNotEmpty()) return extracted

        val items = mutableListOf<CollocationItem>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.opt(key)
            if (value is String) {
                val phrase = value.cleanJsonString()
                val noun = key.cleanJsonString()
                items.add(CollocationItem(noun = noun, collocation = phrase))
            } else if (value is JSONObject) {
                items.add(parseSingleItem(verb, value))
            } else if (value is JSONArray) {
                items.addAll(parseItemsArray(verb, value))
            }
        }
        return items
    }

    private fun parseItemsArray(verb: String, arr: JSONArray): List<CollocationItem> {
        val list = mutableListOf<CollocationItem>()
        for (i in 0 until arr.length()) {
            val item = arr.opt(i) ?: continue
            when (item) {
                is JSONArray -> {
                    val pairItem = parseArrayPair(verb, item)
                    if (pairItem != null) {
                        list.add(pairItem)
                    }
                }
                is JSONObject -> list.add(parseSingleItem(verb, item))
                is String -> {
                    val rawStr = item.trim()
                    if (rawStr.startsWith("[") && rawStr.endsWith("]")) {
                        try {
                            val innerArr = JSONArray(rawStr)
                            val pairItem = parseArrayPair(verb, innerArr)
                            if (pairItem != null) {
                                list.add(pairItem)
                                continue
                            }
                        } catch (_: Exception) {}
                    }
                    list.add(createItem(verb, rawStr))
                }
                else -> list.add(createItem(verb, item.toString()))
            }
        }
        return list
    }

    private fun parseArrayPair(verb: String, arr: JSONArray): CollocationItem? {
        if (arr.length() == 0) return null
        if (arr.length() == 1) {
            return createItem(verb, arr.optString(0))
        }

        val s0 = arr.optString(0).cleanJsonString()
        val s1 = arr.optString(1).cleanJsonString()

        // Distinguish noun and full collocation without changing word order
        val (noun, col) = when {
            // s1 is clearly the longer collocation phrase
            s1.split(" ").size > s0.split(" ").size -> Pair(s0, s1)
            // s0 is clearly the longer collocation phrase
            s0.split(" ").size > s1.split(" ").size -> Pair(s1, s0)
            // If equal length, prefer the one with the verb as collocation
            s1.contains(verb, ignoreCase = true) && !s0.contains(verb, ignoreCase = true) -> Pair(s0, s1)
            s0.contains(verb, ignoreCase = true) && !s1.contains(verb, ignoreCase = true) -> Pair(s1, s0)
            else -> Pair(s0, s1)
        }

        // Collocation is preserved EXACTLY as written in the JSON file
        val finalCollocation = col.ifBlank { noun }
        val finalNoun = if (noun.isNotBlank() && !noun.equals(finalCollocation, ignoreCase = true)) {
            noun
        } else {
            extractNounFromPhrase(finalCollocation, verb)
        }

        return CollocationItem(
            noun = finalNoun,
            collocation = finalCollocation
        )
    }

    private fun parseSingleItem(verb: String, obj: JSONObject): CollocationItem {
        val noun = obj.optString("noun").ifBlank {
            obj.optString("word").ifBlank {
                obj.optString("name")
            }
        }.cleanJsonString()

        val collocation = obj.optString("collocation").ifBlank {
            obj.optString("phrase").ifBlank {
                obj.optString("text")
            }
        }.cleanJsonString()

        val meaning = obj.optString("meaning").ifBlank {
            obj.optString("translation").ifBlank {
                obj.optString("definition")
            }
        }.cleanJsonString()

        val example = obj.optString("example").ifBlank {
            obj.optString("sample")
        }.cleanJsonString()

        val finalCollocation = collocation.ifBlank { noun }
        val finalNoun = if (noun.isNotBlank() && !noun.equals(finalCollocation, ignoreCase = true)) {
            noun
        } else {
            extractNounFromPhrase(finalCollocation, verb)
        }

        return CollocationItem(
            noun = finalNoun,
            collocation = finalCollocation,
            meaning = meaning,
            example = example
        )
    }

    private fun createItem(verb: String, text: String): CollocationItem {
        val cleaned = text.cleanJsonString()
        val noun = extractNounFromPhrase(cleaned, verb)
        return CollocationItem(
            noun = noun,
            collocation = cleaned
        )
    }

    /**
     * Extracts the key noun associated with the verb from the collocation phrase.
     * E.g.: "report acknowledge sth" -> "report"
     *       "abandon an allegiance" -> "allegiance"
     *       "abandon a baby" -> "baby"
     */
    private fun extractNounFromPhrase(phrase: String, verb: String): String {
        val trimmed = phrase.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        if (words.size == 1) return ""

        val verbLower = verb.lowercase().trim()
        val stopWords = setOf(
            "a", "an", "the", "to", "of", "in", "for", "on", "with",
            "at", "by", "from", "up", "about", "into", "over", "after",
            "sth", "sb", "somebody", "something", "that", "as", "it"
        )

        // Find the index of the verb in the phrase
        val verbIdx = words.indexOfFirst {
            val w = it.lowercase().filter { char -> char.isLetter() }
            w == verbLower || w.startsWith(verbLower)
        }

        val candidates = words.filterIndexed { index, word ->
            index != verbIdx && word.lowercase().filter { it.isLetter() } !in stopWords
        }

        return candidates.firstOrNull()?.filter { it.isLetter() } ?: ""
    }

    private fun String.cleanJsonString(): String {
        return this.removePrefix("[").removeSuffix("]").replace("\"", "").trim()
    }
}
