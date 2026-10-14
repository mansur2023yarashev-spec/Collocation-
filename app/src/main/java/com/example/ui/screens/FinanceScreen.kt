package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinanceTransaction
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceScreen(
    transactions: List<FinanceTransaction>,
    onDeleteTransaction: (FinanceTransaction) -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getInstance(Locale("uz", "UZ"))
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())

    var filterType by remember { mutableStateOf("HAMMASI") } // HAMMASI, XARAJAT, DAROMAD

    val filteredList = transactions.filter {
        when (filterType) {
            "XARAJAT" -> it.isExpense
            "DAROMAD" -> !it.isExpense
            else -> true
        }
    }

    val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.isExpense }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // Category breakdown for expenses
    val expensesByCategory = transactions
        .filter { it.isExpense }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_tx_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tranzaksiya kiritish")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("finance_lazy_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Balance Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("finance_balance_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Umumiy Qoldiq",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${currencyFormatter.format(balance)} so'm",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (balance >= 0) IncomeGreen else ExpenseRed
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Pill Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = IncomeGreen.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Daromad",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "+${currencyFormatter.format(totalIncome)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = IncomeGreen,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Expense Pill Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ExpenseRed.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Xarajat",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "-${currencyFormatter.format(totalExpense)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ExpenseRed,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expense Categories Breakdown
            if (expensesByCategory.isNotEmpty() && totalExpense > 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Xarajatlar taqsimoti",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            expensesByCategory.forEach { (cat, amount) ->
                                val fraction = (amount / totalExpense).toFloat()
                                val percent = (fraction * 100).toInt()

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cat, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                        Text(
                                            "${currencyFormatter.format(amount)} so'm ($percent%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = ExpenseRed,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == "HAMMASI",
                        onClick = { filterType = "HAMMASI" },
                        label = { Text("Barchasi") }
                    )
                    FilterChip(
                        selected = filterType == "XARAJAT",
                        onClick = { filterType = "XARAJAT" },
                        label = { Text("Xarajatlar (-)") }
                    )
                    FilterChip(
                        selected = filterType == "DAROMAD",
                        onClick = { filterType = "DAROMAD" },
                        label = { Text("Daromadlar (+)") }
                    )
                }
            }

            // Transaction items
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tranzaksiyalar mavjud emas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currencyFormatter = currencyFormatter,
                        dateFormat = sdf,
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: FinanceTransaction,
    currencyFormatter: NumberFormat,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (transaction.category) {
        "Oziq-ovqat" -> Icons.Default.Fastfood
        "Transport" -> Icons.Default.DirectionsCar
        "Kommunal" -> Icons.Default.Home
        "Salomatlik" -> Icons.Default.MedicalServices
        "Ta'lim" -> Icons.Default.School
        "Ish haqi" -> Icons.Default.LocalAtm
        else -> if (transaction.isExpense) Icons.Default.ShoppingCart else Icons.Default.AccountBalanceWallet
    }

    val iconBg = if (transaction.isExpense) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)
    val iconColor = if (transaction.isExpense) ExpenseRed else IncomeGreen

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tx_card_${transaction.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.category} • ${dateFormat.format(Date(transaction.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (transaction.isExpense) "-" else "+") + "${currencyFormatter.format(transaction.amount)} so'm",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.isExpense) ExpenseRed else IncomeGreen
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "O'chirish",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
