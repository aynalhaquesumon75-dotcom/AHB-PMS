package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.PurchaseOrderSummary
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbDropdownField
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbProgressBar
import com.example.ui.components.AhbTextField
import com.example.ui.components.DropdownOption
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HeroBannerBrush
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils
import com.example.util.ExportUtils
import kotlinx.coroutines.launch

private enum class BalanceStatusFilter {
    ALL,
    PENDING,
    COMPLETED
}

@Composable
fun ProductionBalanceScreen(
    uiState: PmsUiState,
    onAddProductionForPo: (String) -> Unit,
    onNavigatePOs: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var buyerFilter by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(BalanceStatusFilter.ALL) }

    val buyerOptions = remember(uiState.buyers, strings.all) {
        listOf(DropdownOption("", strings.all)) +
            uiState.buyers.map { DropdownOption(it.buyer.id, it.buyer.name) }
    }

    val filtered = remember(uiState.purchaseOrders, searchQuery, buyerFilter, statusFilter) {
        val q = searchQuery.trim().lowercase()
        uiState.purchaseOrders
            .filter { item ->
                val matchBuyer = buyerFilter.isBlank() || item.po.buyerId == buyerFilter
                val matchStatus = when (statusFilter) {
                    BalanceStatusFilter.ALL -> true
                    BalanceStatusFilter.PENDING -> item.remainingQty > 0
                    BalanceStatusFilter.COMPLETED -> item.remainingQty == 0
                }
                val matchQuery = q.isEmpty() ||
                    item.po.poNumber.lowercase().contains(q) ||
                    item.buyerName.lowercase().contains(q) ||
                    item.styleName.lowercase().contains(q)
                matchBuyer && matchStatus && matchQuery
            }
            .sortedByDescending { it.remainingQty }
    }

    val totalOrder = remember(filtered) { filtered.sumOf { it.po.orderQuantity } }
    val totalProduced = remember(filtered) { filtered.sumOf { it.totalProduced } }
    val totalBalance = remember(filtered) { filtered.sumOf { it.remainingQty } }
    val overallProgress = if (totalOrder > 0) (totalProduced.toFloat() / totalOrder.toFloat()).coerceIn(0f, 1f) else 0f

    val pendingCount = remember(uiState.purchaseOrders) { uiState.purchaseOrders.count { it.remainingQty > 0 } }
    val completedCount = remember(uiState.purchaseOrders) { uiState.purchaseOrders.count { it.remainingQty == 0 } }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val headers = listOf(
                    "PO Number",
                    "Buyer",
                    "Style",
                    "Unit",
                    "Order Qty",
                    "Total Produced",
                    "Today Production",
                    "Production Balance",
                    "Completion %",
                    "Delivery Date"
                )
                val rows = filtered.map { item ->
                    val pct = (item.progressFraction * 100).toInt()
                    listOf(
                        item.po.poNumber,
                        item.buyerName,
                        item.styleName,
                        item.po.unit,
                        item.po.orderQuantity.toString(),
                        item.totalProduced.toString(),
                        item.todayProduced.toString(),
                        item.remainingQty.toString(),
                        "$pct%",
                        item.po.deliveryDate.ifBlank { "—" }
                    )
                }
                val csv = ExportUtils.buildCsvContent(headers, rows)
                val res = ExportUtils.writeTextToUri(context, uri, csv)
                if (res.isSuccess) {
                    onNotify(strings.msgCsvExported)
                } else {
                    Toast.makeText(
                        context,
                        res.exceptionOrNull()?.localizedMessage ?: "Export error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row with CSV Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.balanceTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.balanceSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { csvLauncher.launch("AHB_PMS_Balance_${DateUtils.todayIso()}.csv") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("balance_export_csv_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = strings.exportCsv,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("CSV", color = Color.White)
                }
            }
        }

        // Balance Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(HeroBannerBrush)
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.totalBalanceQty,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = DateUtils.formatNumber(totalBalance),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}% ${strings.completionPct}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    AhbProgressBar(
                        progress = overallProgress,
                        color = if (overallProgress >= 1f) EmeraldSuccess else AmberWarning
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BalanceHeroStat(
                            label = strings.totalOrderQty,
                            value = DateUtils.formatNumber(totalOrder)
                        )
                        BalanceHeroStat(
                            label = strings.totalProduction,
                            value = DateUtils.formatNumber(totalProduced)
                        )
                        BalanceHeroStat(
                            label = strings.totalPOs,
                            value = DateUtils.formatNumber(filtered.size)
                        )
                    }
                }
            }
        }

        // Search, Buyer Filter & Balance Status Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AhbTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = strings.searchPOs,
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = strings.search, tint = TextSecondary)
                        },
                        modifier = Modifier.weight(1.2f),
                        testTag = "balance_search_input"
                    )
                    AhbDropdownField(
                        label = strings.filterByBuyer,
                        selectedLabel = buyerOptions.firstOrNull { it.value == buyerFilter }?.label ?: strings.all,
                        options = buyerOptions,
                        onSelect = { buyerFilter = it },
                        modifier = Modifier.weight(1f),
                        testTag = "balance_buyer_filter"
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = statusFilter == BalanceStatusFilter.ALL,
                            onClick = { statusFilter = BalanceStatusFilter.ALL },
                            label = { Text("${strings.all} (${uiState.purchaseOrders.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("balance_filter_all")
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == BalanceStatusFilter.PENDING,
                            onClick = { statusFilter = BalanceStatusFilter.PENDING },
                            label = { Text("${strings.filterPendingBalance} ($pendingCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberWarning,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("balance_filter_pending")
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == BalanceStatusFilter.COMPLETED,
                            onClick = { statusFilter = BalanceStatusFilter.COMPLETED },
                            label = { Text("${strings.filterCompletedBalance} ($completedCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSuccess,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("balance_filter_completed")
                        )
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                AhbEmptyState(
                    icon = Icons.Default.Inventory2,
                    message = if (uiState.purchaseOrders.isEmpty()) strings.noPOs else strings.noBalancePOs,
                    actionLabel = if (uiState.purchaseOrders.isEmpty()) strings.addPO else null,
                    onAction = onNavigatePOs
                )
            }
        } else {
            items(filtered, key = { it.po.id }) { item ->
                PoBalanceCard(
                    item = item,
                    onAddProduction = { onAddProductionForPo(item.po.id) }
                )
            }
        }
    }
}

@Composable
private fun BalanceHeroStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.78f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PoBalanceCard(
    item: PurchaseOrderSummary,
    onAddProduction: () -> Unit
) {
    val strings = LocalStrings.current
    val pct = (item.progressFraction * 100).toInt()
    val isCompleted = item.remainingQty == 0

    AhbCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_card_${item.po.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElectricBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.po.poNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkyBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) EmeraldSuccess else BrightBlue
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
                if (item.po.deliveryDate.isNotBlank()) {
                    Text(
                        text = "${strings.deliveryDate}: ${item.po.deliveryDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = if (isCompleted) EmeraldSuccess.copy(alpha = 0.16f) else AmberWarning.copy(alpha = 0.18f),
                border = BorderStroke(
                    1.dp,
                    if (isCompleted) EmeraldSuccess.copy(alpha = 0.5f) else AmberWarning.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isCompleted) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) strings.statusCompleted else strings.statusPending,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) EmeraldSuccess else AmberWarning
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        AhbProgressBar(
            progress = item.progressFraction,
            color = if (isCompleted) EmeraldSuccess else BrightBlue
        )
        Spacer(Modifier.height(12.dp))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = NavySurfaceElevated,
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceCell(
                    label = strings.orderQuantity.removeSuffix(" *"),
                    value = DateUtils.formatNumber(item.po.orderQuantity),
                    color = BrightBlue
                )
                BalanceCell(
                    label = strings.producedQty,
                    value = DateUtils.formatNumber(item.totalProduced),
                    color = EmeraldSuccess
                )
                BalanceCell(
                    label = strings.todayProduction,
                    value = DateUtils.formatNumber(item.todayProduced),
                    color = SoftViolet
                )
                BalanceCell(
                    label = strings.poBalanceShort,
                    value = DateUtils.formatNumber(item.remainingQty),
                    color = if (isCompleted) EmeraldSuccess else AmberWarning
                )
            }
        }

        if (!isCompleted) {
            Spacer(Modifier.height(10.dp))
            AhbGradientButton(
                text = strings.addProductionForPo,
                icon = Icons.Default.Add,
                onClick = onAddProduction,
                modifier = Modifier.fillMaxWidth(),
                testTag = "balance_add_prod_${item.po.id}"
            )
        }
    }
}

@Composable
private fun BalanceCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
