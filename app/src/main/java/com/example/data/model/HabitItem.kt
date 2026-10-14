package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val iconKey: String = "water", // water, book, fitness, code, walk, sleep, prayer
    val colorHex: Long = 0xFF2563EB,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val completedDates: String = "", // Comma-separated YYYY-MM-DD
    val targetDaysPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis()
)
