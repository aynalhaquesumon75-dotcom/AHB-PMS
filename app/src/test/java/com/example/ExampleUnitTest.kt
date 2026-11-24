package com.example

import com.example.i18n.AppStringsBn
import com.example.i18n.AppStringsEn
import com.example.util.DateUtils
import com.example.util.ExportUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun csvEscaping_handlesCommasAndQuotes() {
        val csv = ExportUtils.buildCsvContent(
            headers = listOf("Buyer", "Notes"),
            rows = listOf(listOf("H&M, Global", "Batch \"A\""))
        )
        assertTrue(csv.contains("\"H&M, Global\""))
        assertTrue(csv.contains("\"Batch \"\"A\"\"\""))
    }

    @Test
    fun dateUtils_formatsAndValidatesDates() {
        val today = DateUtils.todayIso()
        assertEquals(10, today.length)
        assertEquals(today.substring(0, 7), DateUtils.currentMonthIso())
        assertEquals("00", DateUtils.startOfCurrentMonthIso().takeLast(2).replace("01", "00"))
        assertTrue(DateUtils.startOfCurrentMonthIso().endsWith("-01"))
    }

    @Test
    fun bilingualStrings_areFullyPopulated() {
        assertFalse(AppStringsEn.appName.isBlank())
        assertFalse(AppStringsBn.appName.isBlank())
        assertFalse(AppStringsEn.welcomeTitle.isBlank())
        assertFalse(AppStringsBn.welcomeTitle.isBlank())
        assertFalse(AppStringsEn.balanceTitle.isBlank())
        assertFalse(AppStringsBn.balanceTitle.isBlank())
        assertFalse(AppStringsEn.productionBalance.isBlank())
        assertFalse(AppStringsBn.productionBalance.isBlank())
        assertEquals("AHB PMS", AppStringsEn.appName)
        assertEquals("AHB PMS", AppStringsBn.appName)
    }

    @Test
    fun summaryBalanceQty_neverNegative() {
        val buyerSummary = com.example.ui.BuyerSummary(
            buyer = com.example.data.local.BuyerEntity(id = "b1", name = "Buyer A"),
            styleCount = 1,
            poCount = 2,
            totalOrderQty = 1000,
            totalProducedQty = 620
        )
        assertEquals(380, buyerSummary.balanceQty)

        val styleSummary = com.example.ui.StyleSummary(
            style = com.example.data.local.StyleEntity(id = "s1", buyerId = "b1", styleName = "Tee"),
            buyerName = "Buyer A",
            poCount = 1,
            totalOrderQty = 500,
            totalProducedQty = 600
        )
        assertEquals(0, styleSummary.balanceQty)
    }
}
