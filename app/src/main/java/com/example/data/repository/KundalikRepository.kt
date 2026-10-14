package com.example.data.repository

import com.example.data.db.KundalikDao
import com.example.data.model.FinanceTransaction
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KundalikRepository(private val dao: KundalikDao) {

    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allHabits: Flow<List<HabitItem>> = dao.getAllHabits()
    val allTransactions: Flow<List<FinanceTransaction>> = dao.getAllTransactions()
    val allNotes: Flow<List<NoteItem>> = dao.getAllNotes()

    // Tasks
    suspend fun insertTask(task: TaskItem) = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)

    // Habits
    suspend fun insertHabit(habit: HabitItem) = dao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitItem) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitItem) = dao.deleteHabit(habit)

    suspend fun toggleHabitForDate(habit: HabitItem, dateStr: String) {
        val completedList = if (habit.completedDates.isBlank()) {
            mutableListOf()
        } else {
            habit.completedDates.split(",").map { it.trim() }.toMutableList()
        }

        val isAlreadyDone = completedList.contains(dateStr)
        if (isAlreadyDone) {
            completedList.remove(dateStr)
        } else {
            completedList.add(dateStr)
        }

        // Recalculate streak
        val newStreak = calculateStreak(completedList)
        val bestStreak = maxOf(habit.bestStreak, newStreak)

        val updated = habit.copy(
            completedDates = completedList.joinToString(","),
            streakCount = newStreak,
            bestStreak = bestStreak
        )
        dao.updateHabit(updated)
    }

    private fun calculateStreak(completedDates: List<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val sortedDates = completedDates.sortedDescending()
        var streak = 0
        var checkCal = java.util.Calendar.getInstance()

        // Check if today or yesterday is completed
        val checkToday = sdf.format(checkCal.time)
        if (!sortedDates.contains(checkToday)) {
            // Check yesterday
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val checkYesterday = sdf.format(checkCal.time)
            if (!sortedDates.contains(checkYesterday)) {
                return 0
            }
        }

        // Count consecutive days backwards
        while (true) {
            val formatted = sdf.format(checkCal.time)
            if (sortedDates.contains(formatted)) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    // Finance
    suspend fun insertTransaction(transaction: FinanceTransaction) = dao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = dao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)

    // Notes
    suspend fun insertNote(note: NoteItem) = dao.insertNote(note)
    suspend fun updateNote(note: NoteItem) = dao.updateNote(note)
    suspend fun deleteNote(note: NoteItem) = dao.deleteNote(note)

    // Initial Starter Data
    suspend fun populateInitialDataIfEmpty(
        tasksCount: Int,
        habitsCount: Int,
        txCount: Int,
        notesCount: Int
    ) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date(now))

        if (tasksCount == 0) {
            dao.insertTask(
                TaskItem(
                    title = "Bugungi rejalarni ko'rib chiqish",
                    description = "Kun tartibi va muhim uchrashuvlarni belgilash",
                    category = TaskCategory.ISH.name,
                    priority = TaskPriority.YUQORI.name,
                    dueDate = now,
                    isCompleted = true
                )
            )
            dao.insertTask(
                TaskItem(
                    title = "Ingliz tili darsi (1 bob)",
                    description = "Yangi 15 ta so'z yodlash va mashq bajarish",
                    category = TaskCategory.OQISH.name,
                    priority = TaskPriority.ORTA.name,
                    dueDate = now + 3600000 * 4,
                    isCompleted = false
                )
            )
            dao.insertTask(
                TaskItem(
                    title = "Kechki yugurish yoki sayr",
                    description = "Kamida 30 daqiqa toza havoda harakatlanish",
                    category = TaskCategory.SALOMATLIK.name,
                    priority = TaskPriority.ORTA.name,
                    dueDate = now + 3600000 * 8,
                    isCompleted = false
                )
            )
            dao.insertTask(
                TaskItem(
                    title = "Do'kondan oziq-ovqat xarid qilish",
                    description = "Non, mevalar va sut mahsulotlari",
                    category = TaskCategory.XARID.name,
                    priority = TaskPriority.PAST.name,
                    dueDate = now + 3600000 * 6,
                    isCompleted = false
                )
            )
        }

        if (habitsCount == 0) {
            dao.insertHabit(
                HabitItem(
                    title = "2 litr toza suv ichish",
                    description = "Kunning har 2 soatida 1 stakan suv",
                    iconKey = "water",
                    colorHex = 0xFF0284C7,
                    streakCount = 3,
                    bestStreak = 7,
                    completedDates = todayStr,
                    targetDaysPerWeek = 7
                )
            )
            dao.insertHabit(
                HabitItem(
                    title = "20 sahifa kitob mutolaasi",
                    description = "Shaxsiy rivojlanish va dunyoqarash uchun",
                    iconKey = "book",
                    colorHex = 0xFF8B5CF6,
                    streakCount = 5,
                    bestStreak = 12,
                    completedDates = todayStr,
                    targetDaysPerWeek = 7
                )
            )
            dao.insertHabit(
                HabitItem(
                    title = "Ertalabki yengil badantarbiya",
                    description = "15 daqiqa qizish va cho'zilish mashqlari",
                    iconKey = "fitness",
                    colorHex = 0xFF10B981,
                    streakCount = 2,
                    bestStreak = 5,
                    completedDates = "",
                    targetDaysPerWeek = 6
                )
            )
            dao.insertHabit(
                HabitItem(
                    title = "Dasturlash / yangi kod yozish",
                    description = "Kuniga 1 soat diqqat bilan amaliyot",
                    iconKey = "code",
                    colorHex = 0xFFF59E0B,
                    streakCount = 4,
                    bestStreak = 10,
                    completedDates = todayStr,
                    targetDaysPerWeek = 5
                )
            )
        }

        if (txCount == 0) {
            dao.insertTransaction(
                FinanceTransaction(
                    title = "Oylik maosh / Avans",
                    amount = 5500000.0,
                    isExpense = false,
                    category = "Ish haqi",
                    timestamp = now - 86400000 * 2,
                    note = "Kompaniyadan tushum"
                )
            )
            dao.insertTransaction(
                FinanceTransaction(
                    title = "Tushlik va qahva",
                    amount = 45000.0,
                    isExpense = true,
                    category = "Oziq-ovqat",
                    timestamp = now - 3600000 * 3,
                    note = "Kafeda tushlik"
                )
            )
            dao.insertTransaction(
                FinanceTransaction(
                    title = "Taksi yo'l kira",
                    amount = 22000.0,
                    isExpense = true,
                    category = "Transport",
                    timestamp = now - 3600000 * 5,
                    note = "Ishga borish"
                )
            )
        }

        if (notesCount == 0) {
            dao.insertNote(
                NoteItem(
                    title = "Kelgusi oylik maqsadlar 🎯",
                    content = "1. Yangi loyihani ishga tushirish\n2. Ingliz tili B2 darajasi\n3. Har kuni kamida 8000 qadam yurish\n4. Haftalik xarajatlarni qat'iy nazorat qilish",
                    colorIndex = 0,
                    isPinned = true,
                    category = "Maqsadlar"
                )
            )
            dao.insertNote(
                NoteItem(
                    title = "Foydali kitoblar ro'yxati 📚",
                    content = "• Atom odatlar (Jeyms Klir)\n• Diqqat: Chalg'ituvchi dunyoda muvaffaqiyat sirlari (Kel Nyuport)\n• Boy ota, kambag'al ota (Robert Kiyosaki)",
                    colorIndex = 1,
                    isPinned = false,
                    category = "Kitoblar"
                )
            )
        }
    }
}
