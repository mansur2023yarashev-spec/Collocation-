package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val isExpense: Boolean, // true = xarajat, false = daromad
    val category: String, // Oziq-ovqat, Transport, Kommunal, Ta'lim, Ish haqi, Boshqa
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
