package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val label: String, val level: Int) {
    PAST("Past", 1),
    ORTA("O'rta", 2),
    YUQORI("Yuqori", 3)
}

enum class TaskCategory(val label: String, val iconName: String) {
    ISH("Ish", "work"),
    OQISH("O'qish", "school"),
    SHAXSIY("Shaxsiy", "person"),
    SALOMATLIK("Salomatlik", "fitness"),
    XARID("Xarid", "shopping")
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.SHAXSIY.name,
    val priority: String = TaskPriority.ORTA.name,
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
