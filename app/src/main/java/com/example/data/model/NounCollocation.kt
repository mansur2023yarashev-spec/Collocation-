package com.example.data.model

data class NounGroup(
    val noun: String,
    val items: List<CollocationItem>
) {
    /**
     * Returns a list of distinct verbs associated with this noun.
     */
    fun distinctVerbs(): List<String> {
        return items.mapNotNull { item ->
            val v = item.resolveVerb(noun).trim()
            v.ifBlank { null }
        }.distinct()
    }
}

