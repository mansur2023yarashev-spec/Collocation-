package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NoteBlue
import com.example.ui.theme.NotePink
import com.example.ui.theme.NotePurple

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, priority: String, dueDate: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.SHAXSIY.name) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.ORTA.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Yangi Vazifa Qo'shish", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Vazifa nomi *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Izoh (ixtiyoriy)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input")
                )

                Text("Kategoriya:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.label) }
                        )
                    }
                }

                Text("Muhimlik darajasi:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskPriority.values().forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio.name,
                            onClick = { selectedPriority = prio.name },
                            label = { Text(prio.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, selectedCategory, selectedPriority, System.currentTimeMillis())
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("Qo'shish")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, iconKey: String, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("water") }
    var selectedColor by remember { mutableStateOf(0xFF2563EB) }

    val habitIcons = listOf(
        "water" to Icons.Default.WaterDrop,
        "book" to Icons.Default.Book,
        "fitness" to Icons.Default.FitnessCenter,
        "walk" to Icons.Default.DirectionsRun,
        "code" to Icons.Default.Terminal,
        "sleep" to Icons.Default.NightlightRound
    )

    val habitColors = listOf(
        0xFF2563EB, // Blue
        0xFF10B981, // Green
        0xFFF59E0B, // Amber
        0xFF8B5CF6, // Purple
        0xFFEC4899, // Pink
        0xFF0D9488  // Teal
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Yangi Odat Qo'shish", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Odat nomi (masalan: 2L suv ichish) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Izoh yoki maqsad") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Belgi tanlang:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    habitIcons.forEach { (key, iconVec) ->
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = key,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text("Rang tanlang:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    habitColors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorVal },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, selectedIcon, selectedColor)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text("Qo'shish")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, isExpense: Boolean, category: String, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Oziq-ovqat") }
    var note by remember { mutableStateOf("") }

    val expenseCategories = listOf("Oziq-ovqat", "Transport", "Kommunal", "Ko'ngilochar", "Salomatlik", "Boshqa")
    val incomeCategories = listOf("Ish haqi", "Mukofot", "Biznes", "Sovg'a", "Boshqa")

    val categories = if (isExpense) expenseCategories else incomeCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isExpense) "Xarajat Kiritish" else "Daromad Kiritish", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpense) ExpenseRed else Color.Transparent)
                            .clickable {
                                isExpense = true
                                selectedCategory = "Oziq-ovqat"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Xarajat (-)",
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isExpense) IncomeGreen else Color.Transparent)
                            .clickable {
                                isExpense = false
                                selectedCategory = "Ish haqi"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Daromad (+)",
                            fontWeight = FontWeight.Bold,
                            color = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) amountStr = it },
                    label = { Text("Miqdor (so'mda) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nomi (masalan: Tushlik, Oylik) *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_title_input")
                )

                Text("Kategoriya:", style = MaterialTheme.typography.labelLarge)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Qo'shimcha izoh") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val amount = amountStr.toDoubleOrNull()
            Button(
                onClick = {
                    if (title.isNotBlank() && amount != null && amount > 0) {
                        onConfirm(title, amount, isExpense, selectedCategory, note)
                    }
                },
                enabled = title.isNotBlank() && amountStr.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) ExpenseRed else IncomeGreen
                ),
                modifier = Modifier.testTag("save_tx_button")
            ) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, colorIndex: Int, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf("Fikrlar") }

    val noteColorBoxes = listOf(
        Color(0xFFFEF3C7), // Warm Yellow
        Color(0xFFDBEAFE), // Light Blue
        Color(0xFFD1FAE5), // Mint Green
        Color(0xFFEDE9FE), // Lavender
        Color(0xFFFCE7F3)  // Soft Rose
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Yangi Eslatma", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sarlavha *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Eslatma matni *") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Text("Karta rangi:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    noteColorBoxes.forEachIndexed { index, col ->
                        val isSelected = colorIndex == index
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(col)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { colorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, colorIndex, category)
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Saqlash")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
