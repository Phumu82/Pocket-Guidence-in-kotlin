package com.pocketguidance.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    fun formatCurrency(amount: Double, currency: String = "R"): String =
        "$currency${numberFormat.format(amount)}"

    fun todayIso(): String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

    fun firstDayOfMonthIso(): String =
        LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE)

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_DATE)
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } catch (e: Exception) {
            isoDate
        }
    }
}
