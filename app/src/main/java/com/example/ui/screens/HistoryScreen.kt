package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.PmsUiState
import com.example.ui.ProductionEntryDetail
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbDatePickerField
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbTextField
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils
import com.example.util.ExportUtils
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    uiState: PmsUiState,
    onEditEntry: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<ProductionEntryDetail?>(null) }

    val poRemainingMap = remember(uiState.purchaseOrders) {
        uiState.purchaseOrders.associate { it.po.id to it.remainingQty }
    }

    val filtered = remember(uiState.productionEntries, searchQuery, dateFilter) {
        val q = searchQuery.trim().lowercase()
        uiState.productionEntries.filter { item ->
            val matchDate = dateFilter.isBlank() || item.entry.productionDate == dateFilter
            val matchQuery = q.isEmpty() ||
                item.buyerName.lowercase().contains(q) ||
                item.styleName.lowercase().contains(q) ||
                item.poNumber.lowercase().contains(q) ||
                item.lineName.lowercase().contains(q) ||
                item.entry.workerName.lowercase().contains(q)
            matchDate && matchQuery
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val headers = listOf("Date", "Buyer", "Style", "PO Number", "Line", "Worker", "Target", "Today Production", "PO Balance")
                val rows = filtered.map { d ->
                    val poBal = poRemainingMap[d.entry.poId] ?: 0
                    listOf(
                        d.entry.productionDate,
                        d.buyerName,
                        d.styleName,
                        d.poNumber,
                        d.lineName.ifBlank { "—" },
                        d.entry.workerName.ifBlank { "—" },
                        d.entry.targetQuantity.toString(),
                        d.entry.todayQuantity.toString(),
                        poBal.toString()
                    )
                }
                val csv = ExportUtils.buildCsvContent(headers, rows)
                val res = ExportUtils.writeTextToUri(context, uri, csv)
                val msg = if (res.isSuccess) strings.msgCsvExported else (res.exceptionOrNull()?.localizedMessage ?: "Export failed")
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                    text = strings.productionHistory,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.historySubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AhbTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = strings.searchHistory,
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = strings.search, tint = TextSecondary)
                    },
                    testTag = "history_search_input"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AhbDatePickerField(
                        label = strings.filterDate,
                        dateIso = dateFilter,
                        onDateSelected = { dateFilter = it },
                        modifier = Modifier.weight(1f),
                        testTag = "history_date_filter"
                    )
                    if (dateFilter.isNotBlank()) {
                        OutlinedButton(
                            onClick = { dateFilter = "" },
                            border = BorderStroke(1.dp, NavyBorder),
                            modifier = Modifier.height(52.dp).testTag("history_clear_date_btn")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = strings.clearDate, tint = TextSecondary)
                        }
                    }
                    Button(
                        onClick = {
                            csvLauncher.launch("AHB_PMS_History_${DateUtils.todayIso()}.csv")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        modifier = Modifier.height(52.dp).testTag("history_export_csv_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = strings.exportCsv, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CSV", color = Color.White)
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                AhbEmptyState(
                    icon = Icons.Default.History,
                    message = strings.noHistory
                )
            }
        } else {
            items(filtered, key = { it.entry.id }) { item ->
                AhbCard(modifier = Modifier.fillMaxWidth().testTag("history_card_${item.entry.id}")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrightBlue.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = item.entry.productionDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrightBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.poNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${item.buyerName} · ${item.styleName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val meta = buildList {
                                if (item.lineName.isNotBlank()) add(item.lineName)
                                if (item.entry.workerName.isNotBlank()) add(item.entry.workerName)
                                if (item.entry.targetQuantity > 0) add("${strings.targetQuantity}: ${DateUtils.formatNumber(item.entry.targetQuantity)}")
                            }.joinToString(" · ")
                            if (meta.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(text = meta, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            val currentPoBalance = poRemainingMap[item.entry.poId]
                            if (currentPoBalance != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${strings.orderQuantity.removeSuffix(" *")}: ${DateUtils.formatNumber(item.poOrderQty)} · ${strings.poBalanceShort}: ${DateUtils.formatNumber(currentPoBalance)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (currentPoBalance == 0) EmeraldSuccess else AmberWarning
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "+${DateUtils.formatNumber(item.entry.todayQuantity)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onEditEntry(item.entry.id) },
                                    modifier = Modifier.testTag("history_edit_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = SoftViolet)
                                }
                                IconButton(
                                    onClick = { deleteTarget = item },
                                    modifier = Modifier.testTag("history_delete_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = CoralError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AhbConfirmDialog(
            title = strings.deleteEntryTitle,
            message = "${strings.deleteEntryConfirm}\n(${target.poNumber} · ${target.entry.productionDate})",
            confirmText = strings.delete,
            onConfirm = {
                onDeleteEntry(target.entry.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}
