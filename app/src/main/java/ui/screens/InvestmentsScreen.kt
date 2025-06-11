package com.example.spendsense.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

// Updated ViewModel to handle data operations with URI
class InvestmentViewModel : ViewModel() {
    // Savings amount from Excel
    private val _savingsAmount = MutableStateFlow(0.0)
    val savingsAmount: StateFlow<Double> = _savingsAmount

    // Flag to track if data has been loaded
    private val _dataLoaded = MutableStateFlow(false)
    val dataLoaded: StateFlow<Boolean> = _dataLoaded

    // Investment categories with specific Indian investment options
    val investmentOptions = mapOf(
        "Mutual Funds" to listOf(
            InvestmentOption(
                "SBI Bluechip Fund",
                "Large-cap equity fund with solid track record",
                12.5
            ),
            InvestmentOption(
                "HDFC Mid-Cap Opportunities Fund",
                "Mid-cap focused growth fund",
                15.8
            ),
            InvestmentOption("Axis Long Term Equity Fund", "Tax-saving ELSS fund", 14.2),
            InvestmentOption("Kotak Standard Multicap Fund", "Multi-cap equity fund", 13.7)
        ),
        "Stocks" to listOf(
            InvestmentOption(
                "Reliance Industries (RELIANCE.NSE)",
                "Oil, retail, and telecom conglomerate",
                11.3
            ),
            InvestmentOption("HDFC Bank (HDFCBANK.BSE)", "Leading private sector bank", 16.5),
            InvestmentOption("Infosys (INFY.NSE)", "IT services giant", 14.8),
            InvestmentOption("TCS (TCS.NSE)", "India's largest IT company", 13.9)
        ),
        "Gold & Silver" to listOf(
            InvestmentOption("Physical Gold", "Gold coins or jewelry", 9.2),
            InvestmentOption("Sovereign Gold Bond", "Government-backed gold investment", 8.5),
            InvestmentOption("Gold ETF", "Exchange-traded fund tracking gold prices", 7.8),
            InvestmentOption("Silver ETF", "Exchange-traded fund for silver", 10.2)
        ),
        "Bonds" to listOf(
            InvestmentOption("Government Securities", "Issued by RBI, highly secure", 7.3),
            InvestmentOption("Corporate Bonds (AAA)", "High-rated corporate bonds", 8.9),
            InvestmentOption("Fixed Deposits", "Bank FDs with guaranteed returns", 6.5),
            InvestmentOption("Public Sector Bonds", "Bonds issued by PSUs", 7.8)
        )
    )

    // Method to calculate remaining balance from transactions
    fun calculateSavingsFromTransactions(transactions: List<Transaction>): Double {
        return if (transactions.isNotEmpty()) {
            // Sum of all transaction amounts (positive = income, negative = expense)
            val totalAmount =transactions.last().savings
            totalAmount
        } else {
            0.0
        }
    }

    // Method to update savings amount
    fun updateSavingsAmount(amount: Double) {
        _savingsAmount.value = amount
    }

    // Method to set data loaded state
    fun setDataLoaded(loaded: Boolean) {
        _dataLoaded.value = loaded
    }

    // Read savings amount from Excel file using URI
    // Updated method to read the last cell value from Excel file
    // Updated method to read cell value from row 6 in Excel file
    suspend fun readSavingsFromExcel(context: Context, fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("InvestmentDebug", "Processing Excel file: $fileUri")

                val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
                if (inputStream != null) {
                    val workbook = WorkbookFactory.create(inputStream)
                    val sheet = workbook.getSheetAt(0)

                    // Target row 6 (index 5, since rows are 0-indexed)
                    val targetRowIndex = 6
                    val targetRow = sheet.getRow(targetRowIndex)

                    if (targetRow != null) {
                        // Get the last cell in the row (assuming it contains the balance)
                        val lastCellNum = targetRow.lastCellNum - 1
                        val lastCell = targetRow.getCell(lastCellNum.toInt())

                        // Extract the value from the last cell
                        val finalBalance = when {
                            lastCell != null -> {
                                try {
                                    lastCell.numericCellValue
                                } catch (e: Exception) {
                                    try {
                                        lastCell.stringCellValue.replace("[^0-9.]".toRegex(), "")
                                            .toDoubleOrNull() ?: 0.0
                                    } catch (e2: Exception) {
                                        Log.e(
                                            "InvestmentDebug",
                                            "Could not parse cell value from row 6",
                                            e2
                                        )
                                        0.0
                                    }
                                }
                            }

                            else -> 0.0
                        }

                        // Update the savings amount with the value from row 6
                        _savingsAmount.value = finalBalance
                        Log.d(
                            "InvestmentDebug",
                            "Balance from row 6: ${_savingsAmount.value}"
                        )
                    } else {
                        Log.e("InvestmentDebug", "Row 6 not found in Excel file")
                        _savingsAmount.value = 0.0
                    }

                    _dataLoaded.value = true
                    workbook.close()
                    inputStream.close()
                } else {
                    Log.e("InvestmentDebug", "InputStream is null. File may not be accessible.")
                    _savingsAmount.value = 0.0
                    _dataLoaded.value = true
                }
            } catch (e: Exception) {
                Log.e("InvestmentDebug", "Error while reading Excel file", e)
                // Set fallback value in case of an error
                _savingsAmount.value = 0.0
                _dataLoaded.value = true
            }
        }
    }
}
// Data class for investment options
data class InvestmentOption(
    val name: String,
    val description: String,
    val expectedReturn: Double
)

@Composable
fun InvestmentsScreen(
    transactionsViewModel: TransactionsViewModel = viewModel(),
    investmentViewModel: InvestmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val transactions by transactionsViewModel.transactions.collectAsState()
    val dataLoaded by transactionsViewModel.dataLoaded.collectAsState()
    val savingsAmount by investmentViewModel.savingsAmount.collectAsState()
    val investmentDataLoaded by investmentViewModel.dataLoaded.collectAsState()

    // Background color matching TransactionsScreen
    val backgroundColor = Color(0xFFB9B0E5) // Light purple background
    val headerColor = Color(0xFF5E35B1) // Darker purple for header

    // Calculate savings based on transactions whenever they change
    LaunchedEffect(key1 = transactions) {
        if (transactions.isNotEmpty()) {
            val calculatedSavings = investmentViewModel.calculateSavingsFromTransactions(transactions)
            // Update only if we haven't loaded from Excel directly
            if (!investmentDataLoaded) {
                investmentViewModel.updateSavingsAmount(calculatedSavings)
                investmentViewModel.setDataLoaded(true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // PROMINENT INVESTMENTS HEADING (matches style of TransactionsScreen)
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
                        text = "INVESTMENTS",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Secondary header with info (matches style of TransactionsScreen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF7B61FF))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available for Investment",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }

            if (!dataLoaded || transactions.isEmpty()) {
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
                            text = "No transaction data available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please load transaction data from the home screen first",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Savings amount card
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shadowElevation = 4.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.DarkGray)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Current Balance",
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "₹${String.format("%.2f", savingsAmount)}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (savingsAmount >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Investment Suggestions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Investment categories
                    items(investmentViewModel.investmentOptions.keys.toList()) { category ->
                        InvestmentCategoryCard(
                            category = category,
                            options = investmentViewModel.investmentOptions[category] ?: emptyList()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentCategoryCard(category: String, options: List<InvestmentOption>) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            // Category header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White  // Explicitly set to Black instead of DarkGray
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White  // Explicit black tint
                )
            }

            // Expandable content with investment options
            if (expanded) {
                options.forEach { option ->
                    Divider(color = Color(0xFFE0E0E0))
                    InvestmentOptionItem(option)
                }
            }
        }
    }
}

@Composable
fun InvestmentOptionItem(option: InvestmentOption) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = option.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6200EE)  // Keep purple for option names
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = option.description,
            fontSize = 14.sp,
            color = Color.White  // Changed from DarkGray to Black for consistency
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Expected annual return:",
                fontSize = 14.sp,
                color = Color.White  // Changed from DarkGray to Black
            )
            Text(
                text = "${option.expectedReturn}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (option.expectedReturn > 10.0) Color(0xFF4CAF50) else Color(0xFF2196F3)
            )
        }
    }
}