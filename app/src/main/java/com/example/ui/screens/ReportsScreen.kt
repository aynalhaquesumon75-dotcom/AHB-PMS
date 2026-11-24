package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.ChartPoint
import com.example.ui.PmsUiState
import com.example.ui.ReportRow
import com.example.ui.ReportSort
import com.example.ui.ReportType
import com.example.ui.components.AhbBarChart
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbDatePickerField
import com.example.ui.components.AhbDropdownField
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbTextField
import com.example.ui.components.DropdownOption
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils
import com.example.util.ExportUtils
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(
    uiState: PmsUiState,
    buildReportRows: (ReportType, String, String, String, ReportSort) -> Pair<List<ReportRow>, Int>,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var reportType by remember { mutableStateOf(ReportType.PO_WISE) }
    var startDate by remember { mutableStateOf(DateUtils.startOfCurrentMonthIso()) }
    var endDate by remember { mutableStateOf(DateUtils.todayIso()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(ReportSort.QTY_DESC) }
    var showPrintPreview by remember { mutableStateOf(false) }

    val reportTabs = listOf(
        ReportType.DAILY to strings.reportDaily,
        ReportType.MONTHLY to strings.reportMonthly,
        ReportType.PO_WISE to strings.reportPoWise,
        ReportType.BUYER_WISE to strings.reportBuyerWise,
        ReportType.STYLE_WISE to strings.reportStyleWise,
        ReportType.LINE_WISE to strings.reportLineWise,
        ReportType.WORKER_WISE to strings.reportWorkerWise
    )
    val reportTitle = reportTabs.firstOrNull { it.first == reportType }?.second ?: strings.reportsTitle

    val sortOptions = listOf(
        DropdownOption(ReportSort.QTY_DESC, strings.sortQtyDesc),
        DropdownOption(ReportSort.QTY_ASC, strings.sortQtyAsc),
        DropdownOption(ReportSort.NAME_ASC, strings.sortNameAsc)
    )

    val (rows, entryCount) = remember(uiState, reportType, startDate, endDate, searchQuery, sortOrder) {
        buildReportRows(reportType, startDate, endDate, searchQuery, sortOrder)
    }
    val totalQty = remember(rows) { rows.sumOf { it.quantity } }
    val chartPoints = remember(rows) {
        rows.take(10).map { ChartPoint(it.primaryLabel.take(10), it.quantity) }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val headers = listOf("Group", "Detail", "Info", "Entries", "Target/Order", "Production", "Production Balance")
                val csvRows = rows.map {
                    listOf(
                        it.primaryLabel,
                        it.secondaryLabel,
                        it.tertiaryLabel,
                        it.entriesCount.toString(),
                        it.targetOrOrderQty.toString(),
                        it.quantity.toString(),
                        it.remainingQty.toString()
                    )
                }
                val csv = ExportUtils.buildCsvContent(headers, csvRows)
                val res = ExportUtils.writeTextToUri(context, uri, csv)
                if (res.isSuccess) {
                    onNotify(strings.msgCsvExported)
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage ?: "Export error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = strings.reportsTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.reportsSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // 7 Report Type Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reportTabs) { (type, label) ->
                    FilterChip(
                        selected = reportType == type,
                        onClick = { reportType = type },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("report_tab_${type.name.lowercase()}")
                    )
                }
            }
        }

        // Date Range, Presets, Search & Sort
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AhbDatePickerField(
                            label = strings.startDate,
                            dateIso = startDate,
                            onDateSelected = { startDate = it },
                            modifier = Modifier.weight(1f),
                            testTag = "report_start_date"
                        )
                        AhbDatePickerField(
                            label = strings.endDate,
                            dateIso = endDate,
                            onDateSelected = { endDate = it },
                            modifier = Modifier.weight(1f),
                            testTag = "report_end_date"
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val today = DateUtils.todayIso()
                                startDate = today
                                endDate = today
                            },
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.presetToday, maxLines = 1, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = DateUtils.startOfCurrentMonthIso()
                                endDate = DateUtils.todayIso()
                            },
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.presetThisMonth, maxLines = 1, color = TextSecondary)
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = ""
                                endDate = ""
                            },
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.presetAllTime, maxLines = 1, color = TextSecondary)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AhbTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = strings.search,
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                            modifier = Modifier.weight(1.2f),
                            testTag = "report_search_input"
                        )
                        AhbDropdownField(
                            label = "Sort",
                            selectedLabel = sortOptions.firstOrNull { it.value == sortOrder }?.label ?: "",
                            options = sortOptions,
                            onSelect = { sortOrder = it },
                            modifier = Modifier.weight(1f),
                            testTag = "report_sort_dropdown"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                csvLauncher.launch("AHB_PMS_${reportType.name}_${DateUtils.todayIso()}.csv")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("report_export_csv_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = strings.exportCsv, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.exportCsv, color = Color.White, maxLines = 1)
                        }
                        AhbGradientButton(
                            text = strings.printReport,
                            icon = Icons.Default.Print,
                            onClick = { showPrintPreview = true },
                            modifier = Modifier.weight(1f),
                            testTag = "report_print_btn"
                        )
                    }
                }
            }
        }

        // Summary KPI Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportMiniStat(strings.reportTotalOutput, DateUtils.formatNumber(totalQty), BrightBlue, Modifier.weight(1f))
                ReportMiniStat(strings.reportEntriesCount, DateUtils.formatNumber(entryCount), EmeraldSuccess, Modifier.weight(1f))
                ReportMiniStat(strings.reportGroupCount, DateUtils.formatNumber(rows.size), SoftViolet, Modifier.weight(1f))
            }
        }

        // Report Chart
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$reportTitle (${strings.productionOverview})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                AhbBarChart(
                    points = chartPoints,
                    emptyMessage = strings.noReportData
                )
            }
        }

        // Report Rows
        if (rows.isEmpty()) {
            item {
                AhbEmptyState(
                    icon = Icons.Default.BarChart,
                    message = strings.noReportData
                )
            }
        } else {
            items(rows) { row ->
                AhbCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = row.primaryLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${row.secondaryLabel} · ${row.tertiaryLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val showBalance = reportType == ReportType.PO_WISE ||
                                reportType == ReportType.BUYER_WISE ||
                                reportType == ReportType.STYLE_WISE ||
                                row.remainingQty > 0
                            if (row.targetOrOrderQty > 0) {
                                Text(
                                    text = "${strings.orderQuantity.removeSuffix(" *")}: ${DateUtils.formatNumber(row.targetOrOrderQty)}" +
                                        if (showBalance) " · ${strings.poBalanceShort}: ${DateUtils.formatNumber(row.remainingQty)}" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftViolet
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = DateUtils.formatNumber(row.quantity),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${row.entriesCount} ${strings.reportEntriesCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPrintPreview) {
        val dateRangeLabel = if (startDate.isBlank() && endDate.isBlank()) {
            strings.presetAllTime
        } else {
            "${startDate.ifBlank { "…" }} → ${endDate.ifBlank { "…" }}"
        }
        AlertDialog(
            onDismissRequest = { showPrintPreview = false },
            containerColor = NavySurfaceElevated,
            title = { Text(strings.printPreviewTitle) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AHB PMS — $reportTitle",
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateRangeLabel,
                        color = Color(0xFF475569),
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    Text(
                        text = "${strings.reportTotalOutput}: ${DateUtils.formatNumber(totalQty)} | ${strings.reportEntriesCount}: $entryCount",
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.labelLarge
                    )
                    HorizontalDivider(color = Color(0xFFCBD5E1))
                    if (rows.isEmpty()) {
                        Text(strings.noReportData, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                    } else {
                        rows.forEach { r ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.primaryLabel, color = Color(0xFF0F172A), style = MaterialTheme.typography.labelLarge)
                                    val sub = if (r.targetOrOrderQty > 0 && (reportType == ReportType.PO_WISE || reportType == ReportType.BUYER_WISE || reportType == ReportType.STYLE_WISE)) {
                                        "${r.secondaryLabel} · ${r.tertiaryLabel} · ${strings.poBalanceShort}: ${DateUtils.formatNumber(r.remainingQty)}"
                                    } else {
                                        "${r.secondaryLabel} · ${r.tertiaryLabel}"
                                    }
                                    Text(sub, color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
                                }
                                Text(DateUtils.formatNumber(r.quantity), color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            },
            confirmButton = {
                AhbGradientButton(
                    text = strings.printReport,
                    icon = Icons.Default.Print,
                    onClick = {
                        showPrintPreview = false
                        onNotify(strings.msgPrintStarted)
                        val includeBalance = reportType == ReportType.PO_WISE ||
                            reportType == ReportType.BUYER_WISE ||
                            reportType == ReportType.STYLE_WISE
                        ExportUtils.printReportHtml(
                            context = context,
                            jobName = "AHB_PMS_${reportType.name}_Report",
                            title = reportTitle,
                            subtitle = dateRangeLabel,
                            summaryPairs = listOf(
                                strings.reportTotalOutput to DateUtils.formatNumber(totalQty),
                                strings.reportEntriesCount to DateUtils.formatNumber(entryCount),
                                strings.reportGroupCount to DateUtils.formatNumber(rows.size)
                            ),
                            headers = if (includeBalance) {
                                listOf("Name", "Detail", "Info", "Entries", "Production", "Balance")
                            } else {
                                listOf("Name", "Detail", "Info", "Entries", "Production")
                            },
                            rows = rows.map {
                                if (includeBalance) {
                                    listOf(
                                        it.primaryLabel,
                                        it.secondaryLabel,
                                        it.tertiaryLabel,
                                        it.entriesCount.toString(),
                                        DateUtils.formatNumber(it.quantity),
                                        DateUtils.formatNumber(it.remainingQty)
                                    )
                                } else {
                                    listOf(
                                        it.primaryLabel,
                                        it.secondaryLabel,
                                        it.tertiaryLabel,
                                        it.entriesCount.toString(),
                                        DateUtils.formatNumber(it.quantity)
                                    )
                                }
                            }
                        )
                    },
                    testTag = "confirm_print_button"
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPrintPreview = false },
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Text(strings.close, color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ReportMiniStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AhbCard(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
    }
}
