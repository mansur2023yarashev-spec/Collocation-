package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorIndex: Int = 0, // 0 = Yellow, 1 = Blue, 2 = Green, 3 = Purple, 4 = Pink
    val isPinned: Boolean = false,
    val category: String = "Umumiy",
    val updatedAt: Long = System.currentTimeMillis()
)
