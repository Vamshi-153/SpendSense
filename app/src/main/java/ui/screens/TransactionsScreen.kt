package com.example.spendsense.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(
    val description: String,
    val amount: Double,
    val date: String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date()),
    val savings: Double
)


class TransactionsViewModel : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    // Flag to track if data has been loaded
    private val _dataLoaded = MutableStateFlow(false)
    val dataLoaded: StateFlow<Boolean> = _dataLoaded

    // Function to load transactions from an Excel file
    suspend fun loadTransactionsFromExcel(context: Context, fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("ExcelDebug", "Selected File URI: $fileUri")

                val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                if (inputStream != null) {
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    val transactionsList = mutableListOf<Transaction>()
                    for (row in sheet) {
                        if (row.rowNum == 0) continue // Skip header row

                        try {
                            val description = row.getCell(2)?.stringCellValue ?: "Unknown"
                            val amount = row.getCell(4)?.numericCellValue ?: 0.0
                            val savings=row.getCell(6)?.numericCellValue?:0.0

                            // Try to get date if available
                            val dateCell = row.getCell(0)
                            val date = if (dateCell != null) {
                                try {
                                    val cellDate = dateCell.dateCellValue
                                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(cellDate)
                                } catch (e: Exception) {
                                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
                                }
                            } else {
                                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
                            }

                            transactionsList.add(Transaction(description, amount, date,savings))
                        } catch (e: Exception) {
                            Log.e("ExcelDebug", "Error processing row ${row.rowNum}", e)
                        }
                    }

                    // Update state on Main thread
                    withContext(Dispatchers.Main) {
                        if (transactionsList.isNotEmpty()) {
                            _transactions.value = transactionsList
                            _dataLoaded.value = true
                            Log.d("ExcelDebug", "TransactionsList Updated: ${_transactions.value}")
                        } else {
                            Log.w("ExcelDebug", "No transactions found in the Excel file")
                        }
                    }

                    workbook.close()
                    inputStream.close()
                } else {
                    Log.e("ExcelDebug", "InputStream is null. File may not be accessible.")
                }
            } catch (e: Exception) {
                Log.e("ExcelDebug", "Error while reading Excel file", e)
            }
        }
    }
}

@Composable
fun TransactionsScreen(transactionsViewModel: TransactionsViewModel) {
    // Collect transactions state
    val transactions by transactionsViewModel.transactions.collectAsState()
    val dataLoaded by transactionsViewModel.dataLoaded.collectAsState()

    // Using a purple theme to match the screenshot
    val backgroundColor = Color(0xFFB9B0E5) // Light purple background
    val headerColor = Color(0xFF5E35B1) // Darker purple for header
    val dividerColor = Color(0xFF7B61FF) // Visible purple divider



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // PROMINENT TRANSACTIONS HEADING
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = headerColor
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TRANSACTIONS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!dataLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "No transactions loaded yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please select an Excel file from the home screen",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Excel file processed",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "But no transactions were found")
                        }
                    }
                }
            } else {
                // Secondary header with count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF7B61FF))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Entries: ${transactions.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                // Enhanced transaction list with container and division lines
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(transactions) { index, transaction ->
                        // Transaction container with drop shadow
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            // Inner container for transaction
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.DarkGray)
                            ) {
                                // Category tab
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF81C784))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                )

                                // Transaction details
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left side: Description and date
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = transaction.description,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = transaction.date,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }

                                    // Amount with rupee symbol
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (transaction.amount < 0) Color(0xFFFF5252) else Color(0xFF4CAF50),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (transaction.amount < 0) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = if (transaction.amount < 0) "Expense" else "Income",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "₹${String.format("%.2f", Math.abs(transaction.amount))}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Division line between transactions
                        if (index < transactions.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                Divider(
                                    color = dividerColor,
                                    thickness = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}