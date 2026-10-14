package com.example.data.model

data class AdjectiveGroup(
    val adjective: String,
    val items: List<CollocationItem>
) {
    /**
     * Returns a list of distinct nouns associated with this adjective.
     */
    fun distinctNouns(): List<String> {
        return items.mapNotNull { item ->
            val n = item.noun.trim()
            n.ifBlank { null }
        }.distinct()
    }
}
