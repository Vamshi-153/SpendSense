package com.example.spendsense

import android.content.Context
import android.net.Uri
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

object processTransactionData {
    fun process(context: Context, uri: Uri): Map<String, Double> {
        val categorizedData = mutableMapOf<String, Double>()
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0) // Assume data is in the first sheet

            for (row in sheet) {
                if (row.rowNum == 0) continue // Skip header row

                // Get Category and Amount correctly from columns
                val category = row.getCell(5)?.stringCellValue ?: "Others" // Column F
                val amount = row.getCell(4)?.numericCellValue ?: 0.0      // Column E

                // Add amount to corresponding category
                categorizedData[category] = categorizedData.getOrDefault(category, 0.0) + amount
            }
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return categorizedData
    }
}
