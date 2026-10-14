package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.FinanceTransaction
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.repository.KundalikRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TaskFilter(val title: String) {
    HAMMASI("Barchasi"),
    BUGUN("Bugun"),
    MUHIM("Muhim"),
    BAJARILGAN("Bajarilgan")
}

data class DashboardStats(
    val completedTasksCount: Int = 0,
    val totalTasksCount: Int = 0,
    val taskProgressFraction: Float = 0f,
    val todayCompletedHabitsCount: Int = 0,
    val totalHabitsCount: Int = 0,
    val habitProgressFraction: Float = 0f,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val bestStreak: Int = 0
)

class KundalikViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KundalikRepository

    val allTasks: StateFlow<List<TaskItem>>
    val allHabits: StateFlow<List<HabitItem>>
    val allTransactions: StateFlow<List<FinanceTransaction>>
    val allNotes: StateFlow<List<NoteItem>>

    private val _taskFilter = MutableStateFlow(TaskFilter.HAMMASI)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Filtered tasks
    val filteredTasks: StateFlow<List<TaskItem>>

    // Dashboard Statistics
    val dashboardStats: StateFlow<DashboardStats>

    // Daily Uzbek inspirational quote
    val todayQuote: String

    init {
        val db = AppDatabase.getDatabase(application)
        repository = KundalikRepository(db.kundalikDao())

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allHabits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNotes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val quotes = listOf(
            "“Kichik odatlar — ulkan o'zgarishlarning poydevoridir.”",
            "“Bugungi intizom — ertangi erkinlik va muvaffaqiyatdir.”",
            "“Eng qiyin qadam — boshlash. Boshlagan ishni oxiriga yetkazing!”",
            "“Vaqt — sarflanadigan emas, investitsiya qilinadigan eng qimmatli ne'matdir.”",
            "“Har bir yangi kun — yangi imkoniyat va yangi marralar demakdir.”"
        )
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        todayQuote = quotes[dayOfYear % quotes.size]

        // Combine for filtered tasks
        filteredTasks = combine(allTasks, _taskFilter, _selectedCategory, _searchQuery) { tasks, filter, cat, query ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            tasks.filter { task ->
                val matchesCategory = cat == null || task.category.equals(cat, ignoreCase = true)
                val matchesQuery = query.isBlank() ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    TaskFilter.HAMMASI -> true
                    TaskFilter.BUGUN -> {
                        val taskDateStr = sdf.format(Date(task.dueDate))
                        taskDateStr == todayStr
                    }
                    TaskFilter.MUHIM -> task.priority == TaskPriority.YUQORI.name
                    TaskFilter.BAJARILGAN -> task.isCompleted
                }

                matchesCategory && matchesQuery && matchesFilter
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Combine for stats
        dashboardStats = combine(allTasks, allHabits, allTransactions) { tasks, habits, transactions ->
            val totalTasks = tasks.size
            val doneTasks = tasks.count { it.isCompleted }
            val taskFrac = if (totalTasks > 0) doneTasks.toFloat() / totalTasks else 0f

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val totalHabits = habits.size
            val doneHabitsToday = habits.count { habit ->
                habit.completedDates.split(",").map { it.trim() }.contains(todayStr)
            }
            val habitFrac = if (totalHabits > 0) doneHabitsToday.toFloat() / totalHabits else 0f

            var income = 0.0
            var expense = 0.0
            for (tx in transactions) {
                if (tx.isExpense) {
                    expense += tx.amount
                } else {
                    income += tx.amount
                }
            }

            val maxStreak = habits.maxOfOrNull { it.bestStreak } ?: 0

            DashboardStats(
                completedTasksCount = doneTasks,
                totalTasksCount = totalTasks,
                taskProgressFraction = taskFrac,
                todayCompletedHabitsCount = doneHabitsToday,
                totalHabitsCount = totalHabits,
                habitProgressFraction = habitFrac,
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                bestStreak = maxStreak
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardStats()
        )

        // Seed initial mock data if empty
        viewModelScope.launch {
            val curTasks = repository.allTasks.first()
            val curHabits = repository.allHabits.first()
            val curTx = repository.allTransactions.first()
            val curNotes = repository.allNotes.first()

            if (curTasks.isEmpty() && curHabits.isEmpty() && curTx.isEmpty() && curNotes.isEmpty()) {
                repository.populateInitialDataIfEmpty(
                    curTasks.size,
                    curHabits.size,
                    curTx.size,
                    curNotes.size
                )
            }
        }
    }

    // Filter controls
    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun setSelectedCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Task Actions
    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
        }
    }

    fun addTask(title: String, description: String, category: String, priority: String, dueDate: Long) {
        viewModelScope.launch {
            repository.insertTask(
                TaskItem(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    isCompleted = false
                )
            )
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Habit Actions
    fun toggleHabitToday(habit: HabitItem) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        viewModelScope.launch {
            repository.toggleHabitForDate(habit, todayStr)
        }
    }

    fun addHabit(title: String, description: String, iconKey: String, colorHex: Long) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitItem(
                    title = title.trim(),
                    description = description.trim(),
                    iconKey = iconKey,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteHabit(habit: HabitItem) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Finance Actions
    fun addTransaction(title: String, amount: Double, isExpense: Boolean, category: String, note: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinanceTransaction(
                    title = title.trim(),
                    amount = amount,
                    isExpense = isExpense,
                    category = category,
                    note = note.trim()
                )
            )
        }
    }

    fun deleteTransaction(tx: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    // Note Actions
    fun addNote(title: String, content: String, colorIndex: Int, category: String) {
        viewModelScope.launch {
            repository.insertNote(
                NoteItem(
                    title = title.trim(),
                    content = content.trim(),
                    colorIndex = colorIndex,
                    category = category,
                    isPinned = false
                )
            )
        }
    }

    fun toggleNotePinned(note: NoteItem) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}
