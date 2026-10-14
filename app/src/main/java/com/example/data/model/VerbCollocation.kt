package com.example.data.model

data class CollocationItem(
    val verb: String = "",
    val noun: String = "",
    val adjective: String = "",
    val collocation: String = "",
    val meaning: String = "",
    val example: String = ""
) {
    /**
     * Resolves the primary verb associated with this collocation item.
     */
    fun resolveVerb(headwordNoun: String = ""): String {
        if (verb.isNotBlank()) return verb.trim()
        val targetNoun = noun.ifBlank { headwordNoun }.trim()
        val phrase = collocation.trim()
        if (phrase.isNotBlank()) {
            val tokens = phrase.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (tokens.isNotEmpty()) {
                val candidate = tokens.first().trim()
                if (!candidate.equals(targetNoun, ignoreCase = true)) {
                    if (tokens.size >= 2 && listOf("up", "to", "out", "in", "on", "with", "into", "at", "for", "from").contains(tokens[1].lowercase())) {
                        return "${tokens[0]} ${tokens[1]}"
                    }
                    return candidate
                } else if (tokens.size >= 2) {
                    // Noun comes first: e.g. "report acknowledge sth", "rumour spread"
                    val second = tokens[1].trim()
                    if (!listOf("a", "an", "the", "of", "in", "on", "at", "to", "for", "with").contains(second.lowercase())) {
                        if (tokens.size >= 3 && listOf("up", "to", "out", "in", "on", "with", "into", "at", "for", "from").contains(tokens[2].lowercase())) {
                            return "${tokens[1]} ${tokens[2]}"
                        }
                        return second
                    }
                }
            }
        }
        return ""
    }

    /**
     * Resolves the primary adjective associated with this collocation item.
     */
    fun resolveAdjective(headwordAdjective: String = ""): String {
        if (adjective.isNotBlank()) return adjective.trim()
        val targetAdj = headwordAdjective.trim()
        val phrase = collocation.trim()
        if (targetAdj.isNotBlank() && phrase.contains(targetAdj, ignoreCase = true)) {
            return targetAdj
        }
        if (phrase.isNotBlank()) {
            val tokens = phrase.split(Regex("\\s+"))
            if (tokens.isNotEmpty()) {
                val candidate = tokens.first().trim()
                if (!candidate.equals(noun, ignoreCase = true)) {
                    return candidate
                }
            }
        }
        return targetAdj
    }

    /**
     * Formats the adjective + noun collocation phrase for display.
     */
    fun displayAdjectiveCollocation(headwordAdjective: String = ""): String {
        val effectiveAdj = resolveAdjective(headwordAdjective).ifBlank { headwordAdjective }.trim()
        val effectiveNoun = noun.trim()

        if (collocation.isNotBlank()) {
            val clean = collocation.trim()
            if (!clean.equals(headwordAdjective, ignoreCase = true) && !clean.equals(effectiveAdj, ignoreCase = true)) {
                return clean
            }
        }

        if (effectiveAdj.isNotBlank() && effectiveNoun.isNotBlank()) {
            return formatAdjectiveNounCollocation(effectiveAdj, effectiveNoun)
        }

        return when {
            collocation.isNotBlank() -> collocation.trim()
            effectiveNoun.isNotBlank() -> "$effectiveAdj $effectiveNoun".trim()
            else -> effectiveAdj
        }
    }

    /**
     * Formats the collocation phrase for display.
     */
    fun displayCollocation(headword: String = ""): String {
        val effectiveNoun = noun.ifBlank { headword }.trim()
        val effectiveVerb = resolveVerb(headword).trim()

        if (collocation.isNotBlank()) {
            val clean = collocation.trim()
            if (!clean.equals(headword, ignoreCase = true) && !clean.equals(effectiveNoun, ignoreCase = true)) {
                return clean
            }
        }

        if (effectiveVerb.isNotBlank() && effectiveNoun.isNotBlank()) {
            return formatVerbNounCollocation(effectiveVerb, effectiveNoun)
        }

        return when {
            collocation.isNotBlank() -> collocation.trim()
            effectiveVerb.isNotBlank() -> effectiveVerb
            effectiveNoun.isNotBlank() -> effectiveNoun
            else -> headword
        }
    }

    companion object {
        fun formatVerbNounCollocation(verb: String, noun: String): String {
            val cleanVerb = verb.trim()
            val cleanNoun = noun.trim()
            if (cleanVerb.contains(cleanNoun, ignoreCase = true)) return cleanVerb
            if (cleanNoun.contains(cleanVerb, ignoreCase = true)) return cleanNoun

            val lowerNoun = cleanNoun.lowercase()
            val isPlural = lowerNoun.endsWith("s") && !lowerNoun.endsWith("ss") && !lowerNoun.endsWith("us") && !lowerNoun.endsWith("is")
            val article = if (isPlural) "" else if (isVowelStart(lowerNoun)) "an " else "a "

            return "$cleanVerb $article$cleanNoun"
        }

        fun formatAdjectiveNounCollocation(adjective: String, noun: String): String {
            val cleanAdj = adjective.trim()
            val cleanNoun = noun.trim()
            if (cleanAdj.contains(cleanNoun, ignoreCase = true)) return cleanAdj
            if (cleanNoun.contains(cleanAdj, ignoreCase = true)) return cleanNoun
            return "$cleanAdj $cleanNoun"
        }

        private fun isVowelStart(word: String): Boolean {
            if (word.isEmpty()) return false
            val first = word.first().lowercaseChar()
            return first in listOf('a', 'e', 'i', 'o')
        }
    }
}

data class VerbGroup(
    val verb: String,
    val items: List<CollocationItem>
)

