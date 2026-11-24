package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.PurchaseOrderSummary
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbDatePickerField
import com.example.ui.components.AhbDropdownField
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbProgressBar
import com.example.ui.components.AhbTextField
import com.example.ui.components.DropdownOption
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun PurchaseOrdersScreen(
    uiState: PmsUiState,
    onSavePO: (
        editId: String?,
        buyerId: String,
        styleId: String,
        poNumber: String,
        orderQtyText: String,
        orderDate: String,
        deliveryDate: String,
        unit: String,
        notes: String
    ) -> String?,
    onDeletePO: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var buyerFilter by remember { mutableStateOf("") }
    var styleFilter by remember { mutableStateOf("") }

    var showEditor by remember { mutableStateOf(false) }
    var editingPO by remember { mutableStateOf<PurchaseOrderSummary?>(null) }
    var detailsPO by remember { mutableStateOf<PurchaseOrderSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<PurchaseOrderSummary?>(null) }

    val buyerOptions = remember(uiState.buyers, strings.all) {
        listOf(DropdownOption("", strings.all)) +
            uiState.buyers.map { DropdownOption(it.buyer.id, it.buyer.name) }
    }
    val styleOptions = remember(uiState.styles, buyerFilter, strings.all) {
        val list = if (buyerFilter.isBlank()) uiState.styles else uiState.styles.filter { it.style.buyerId == buyerFilter }
        listOf(DropdownOption("", strings.all)) + list.map { DropdownOption(it.style.id, it.style.styleName) }
    }

    val filtered = remember(uiState.purchaseOrders, searchQuery, buyerFilter, styleFilter) {
        val q = searchQuery.trim().lowercase()
        uiState.purchaseOrders.filter { item ->
            val matchBuyer = buyerFilter.isBlank() || item.po.buyerId == buyerFilter
            val matchStyle = styleFilter.isBlank() || item.po.styleId == styleFilter
            val matchQuery = q.isEmpty() ||
                item.po.poNumber.lowercase().contains(q) ||
                item.buyerName.lowercase().contains(q) ||
                item.styleName.lowercase().contains(q)
            matchBuyer && matchStyle && matchQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = strings.poManagement,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.poSubtitle,
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
                        label = strings.searchPOs,
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = strings.search, tint = TextSecondary)
                        },
                        testTag = "po_search_input"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AhbDropdownField(
                            label = strings.filterByBuyer,
                            selectedLabel = buyerOptions.firstOrNull { it.value == buyerFilter }?.label ?: strings.all,
                            options = buyerOptions,
                            onSelect = {
                                buyerFilter = it
                                styleFilter = ""
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "po_buyer_filter"
                        )
                        AhbDropdownField(
                            label = strings.filterByStyle,
                            selectedLabel = styleOptions.firstOrNull { it.value == styleFilter }?.label ?: strings.all,
                            options = styleOptions,
                            onSelect = { styleFilter = it },
                            modifier = Modifier.weight(1f),
                            testTag = "po_style_filter"
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    AhbEmptyState(
                        icon = Icons.Default.ReceiptLong,
                        message = strings.noPOs,
                        actionLabel = if (uiState.styles.isNotEmpty()) strings.addPO else null,
                        onAction = {
                            editingPO = null
                            showEditor = true
                        }
                    )
                }
            } else {
                items(filtered, key = { it.po.id }) { item ->
                    PurchaseOrderCard(
                        item = item,
                        onDetails = { detailsPO = item },
                        onEdit = {
                            editingPO = item
                            showEditor = true
                        },
                        onDelete = { deleteTarget = item }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingPO = null
                showEditor = true
            },
            containerColor = ElectricBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_po_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addPO)
        }
    }

    if (showEditor) {
        PurchaseOrderEditorDialog(
            initial = editingPO,
            uiState = uiState,
            onDismiss = { showEditor = false },
            onSave = { buyerId, styleId, poNum, orderQty, oDate, dDate, unit, notes ->
                val err = onSavePO(editingPO?.po?.id, buyerId, styleId, poNum, orderQty, oDate, dDate, unit, notes)
                if (err == null) showEditor = false
                err
            }
        )
    }

    detailsPO?.let { item ->
        PurchaseOrderDetailsDialog(item = item, onDismiss = { detailsPO = null })
    }

    deleteTarget?.let { target ->
        AhbConfirmDialog(
            title = strings.deletePOTitle,
            message = "${strings.deletePOConfirm}\n(${target.po.poNumber})",
            confirmText = strings.delete,
            onConfirm = {
                onDeletePO(target.po.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun PurchaseOrderCard(
    item: PurchaseOrderSummary,
    onDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    val pct = (item.progressFraction * 100).toInt()

    AhbCard(modifier = Modifier.fillMaxWidth().testTag("po_card_${item.po.id}")) {
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
                        color = if (pct >= 100) EmeraldSuccess else AmberWarning
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
            Row {
                IconButton(onClick = onDetails, modifier = Modifier.testTag("po_details_btn")) {
                    Icon(Icons.Default.Info, contentDescription = strings.details, tint = BrightBlue)
                }
                IconButton(onClick = onEdit, modifier = Modifier.testTag("po_edit_btn")) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = SoftViolet)
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("po_delete_btn")) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = CoralError)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        AhbProgressBar(
            progress = item.progressFraction,
            color = if (pct >= 100) EmeraldSuccess else BrightBlue
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricMini(strings.orderQuantity.removeSuffix(" *"), "${DateUtils.formatNumber(item.po.orderQuantity)} ${item.po.unit}", BrightBlue)
            MetricMini(strings.producedQty, DateUtils.formatNumber(item.totalProduced), EmeraldSuccess)
            MetricMini(strings.remainingQty, DateUtils.formatNumber(item.remainingQty), AmberWarning)
        }
    }
}

@Composable
private fun MetricMini(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PurchaseOrderEditorDialog(
    initial: PurchaseOrderSummary?,
    uiState: PmsUiState,
    onDismiss: () -> Unit,
    onSave: (
        buyerId: String,
        styleId: String,
        poNumber: String,
        orderQtyText: String,
        orderDate: String,
        deliveryDate: String,
        unit: String,
        notes: String
    ) -> String?
) {
    val strings = LocalStrings.current
    var buyerId by remember { mutableStateOf(initial?.po?.buyerId ?: uiState.buyers.firstOrNull()?.buyer?.id ?: "") }
    val availableStyles = remember(buyerId, uiState.styles) {
        uiState.styles.filter { it.style.buyerId == buyerId }
    }
    var styleId by remember {
        mutableStateOf(
            initial?.po?.styleId ?: availableStyles.firstOrNull()?.style?.id ?: ""
        )
    }
    var poNumber by remember { mutableStateOf(initial?.po?.poNumber ?: "") }
    var orderQty by remember { mutableStateOf(initial?.po?.orderQuantity?.toString() ?: "") }
    var orderDate by remember { mutableStateOf(initial?.po?.orderDate?.ifBlank { DateUtils.todayIso() } ?: DateUtils.todayIso()) }
    var deliveryDate by remember { mutableStateOf(initial?.po?.deliveryDate ?: "") }
    var unit by remember { mutableStateOf(initial?.po?.unit ?: "Pcs") }
    var notes by remember { mutableStateOf(initial?.po?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val buyerOptions = uiState.buyers.map { DropdownOption(it.buyer.id, it.buyer.name) }
    val styleOptions = availableStyles.map { DropdownOption(it.style.id, it.style.styleName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = if (initial == null) strings.addPO else strings.editPO,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                errorMsg?.let {
                    Text(text = it, color = CoralError, style = MaterialTheme.typography.bodySmall)
                }
                AhbDropdownField(
                    label = strings.selectBuyer,
                    selectedLabel = buyerOptions.firstOrNull { it.value == buyerId }?.label ?: "",
                    options = buyerOptions,
                    onSelect = { newBuyer ->
                        buyerId = newBuyer
                        val nextStyles = uiState.styles.filter { it.style.buyerId == newBuyer }
                        styleId = nextStyles.firstOrNull()?.style?.id ?: ""
                        errorMsg = null
                    },
                    testTag = "po_buyer_dropdown"
                )
                AhbDropdownField(
                    label = strings.selectStyle,
                    selectedLabel = styleOptions.firstOrNull { it.value == styleId }?.label ?: "",
                    options = styleOptions,
                    onSelect = { styleId = it; errorMsg = null },
                    testTag = "po_style_dropdown"
                )
                AhbTextField(
                    value = poNumber,
                    onValueChange = { poNumber = it; errorMsg = null },
                    label = strings.poNumber,
                    testTag = "po_number_input"
                )
                AhbTextField(
                    value = orderQty,
                    onValueChange = { input -> orderQty = input.filter { it.isDigit() }; errorMsg = null },
                    label = strings.orderQuantity,
                    keyboardType = KeyboardType.Number,
                    testTag = "po_order_qty_input"
                )
                AhbDatePickerField(
                    label = strings.orderDate,
                    dateIso = orderDate,
                    onDateSelected = { orderDate = it },
                    testTag = "po_order_date_input"
                )
                AhbDatePickerField(
                    label = strings.deliveryDate,
                    dateIso = deliveryDate,
                    onDateSelected = { deliveryDate = it },
                    testTag = "po_delivery_date_input"
                )
                AhbTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = strings.unit,
                    testTag = "po_unit_input"
                )
                AhbTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = strings.notes,
                    singleLine = false,
                    maxLines = 3,
                    testTag = "po_notes_input"
                )
            }
        },
        confirmButton = {
            AhbGradientButton(
                text = if (initial == null) strings.save else strings.update,
                onClick = {
                    val err = onSave(buyerId, styleId, poNumber, orderQty, orderDate, deliveryDate, unit, notes)
                    if (err != null) errorMsg = err
                },
                testTag = "save_po_button"
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Text(strings.cancel, color = TextSecondary)
            }
        }
    )
}

@Composable
private fun PurchaseOrderDetailsDialog(
    item: PurchaseOrderSummary,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val pct = (item.progressFraction * 100).toInt()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = { Text("${strings.poDetails}: ${item.po.poNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${strings.selectBuyer.removeSuffix(" *")}: ${item.buyerName}", style = MaterialTheme.typography.bodyMedium)
                Text("${strings.selectStyle.removeSuffix(" *")}: ${item.styleName}", style = MaterialTheme.typography.bodyMedium)
                Text("${strings.orderQuantity.removeSuffix(" *")}: ${DateUtils.formatNumber(item.po.orderQuantity)} ${item.po.unit}", style = MaterialTheme.typography.bodyMedium)
                Text("${strings.producedQty}: ${DateUtils.formatNumber(item.totalProduced)} ${item.po.unit}", color = EmeraldSuccess, style = MaterialTheme.typography.bodyMedium)
                Text("${strings.remainingQty}: ${DateUtils.formatNumber(item.remainingQty)} ${item.po.unit}", color = AmberWarning, style = MaterialTheme.typography.bodyMedium)
                Text("${strings.completionPct}: $pct%", color = SkyBlue, style = MaterialTheme.typography.bodyMedium)
                if (item.po.orderDate.isNotBlank()) {
                    Text("${strings.orderDate}: ${item.po.orderDate}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                if (item.po.deliveryDate.isNotBlank()) {
                    Text("${strings.deliveryDate}: ${item.po.deliveryDate}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                if (item.po.notes.isNotBlank()) {
                    Text("${strings.notes}: ${item.po.notes}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                AhbProgressBar(progress = item.progressFraction)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}
