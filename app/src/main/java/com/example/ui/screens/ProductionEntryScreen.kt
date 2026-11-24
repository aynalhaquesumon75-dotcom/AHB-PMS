package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.ProductionEntryMode
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbDatePickerField
import com.example.ui.components.AhbDropdownField
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
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun ProductionEntryScreen(
    uiState: PmsUiState,
    calculatePreviousProduction: (poId: String, date: String, excludeId: String?) -> Int,
    calculateTotalProducedOnPo: (poId: String, excludeId: String?) -> Int,
    onSaveProduction: (
        editId: String?,
        date: String,
        poId: String,
        lineId: String?,
        workerName: String,
        targetQtyText: String,
        mode: ProductionEntryMode,
        inputValueText: String
    ) -> String?,
    onCancelEdit: () -> Unit,
    onConsumePreselectedPo: () -> Unit = {},
    onNavigatePOs: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onNavigateBalance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    val editing = uiState.editingEntry

    var selectedDate by remember { mutableStateOf(DateUtils.todayIso()) }
    var selectedPoId by remember { mutableStateOf("") }
    var selectedLineId by remember { mutableStateOf("") }
    var workerName by remember { mutableStateOf("") }
    var targetQtyText by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(ProductionEntryMode.CUMULATIVE_TOTAL) }
    var quantityInputText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Preselect PO when launched from Production Balance screen
    LaunchedEffect(uiState.preselectedPoId) {
        val targetPo = uiState.preselectedPoId
        if (editing == null && !targetPo.isNullOrBlank()) {
            selectedPoId = targetPo
            errorMsg = null
            onConsumePreselectedPo()
        }
    }

    // Synchronize form when editing an existing entry
    LaunchedEffect(editing?.entry?.id) {
        if (editing != null) {
            selectedDate = editing.entry.productionDate
            selectedPoId = editing.entry.poId
            selectedLineId = editing.entry.lineId ?: ""
            workerName = editing.entry.workerName
            targetQtyText = if (editing.entry.targetQuantity > 0) editing.entry.targetQuantity.toString() else ""
            val before = calculatePreviousProduction(editing.entry.poId, editing.entry.productionDate, editing.entry.id)
            quantityInputText = if (mode == ProductionEntryMode.CUMULATIVE_TOTAL) {
                (before + editing.entry.todayQuantity).toString()
            } else {
                editing.entry.todayQuantity.toString()
            }
            errorMsg = null
        }
    }

    val selectedPoSummary = uiState.purchaseOrders.firstOrNull { it.po.id == selectedPoId }
    val poOptions = uiState.purchaseOrders.map {
        DropdownOption(it.po.id, "${it.po.poNumber} · ${it.buyerName} (${it.styleName})")
    }
    val lineOptions = listOf(DropdownOption("", strings.optionalLine)) +
        uiState.lines.map { DropdownOption(it.line.id, it.line.lineName) }

    val previousQty = remember(selectedPoId, selectedDate, editing?.entry?.id, uiState.productionEntries) {
        calculatePreviousProduction(selectedPoId, selectedDate, editing?.entry?.id)
    }
    val allOtherEntriesOnPo = remember(selectedPoId, editing?.entry?.id, uiState.productionEntries) {
        calculateTotalProducedOnPo(selectedPoId, editing?.entry?.id)
    }

    val parsedInput = quantityInputText.trim().toIntOrNull() ?: 0
    val calculatedToday = when (mode) {
        ProductionEntryMode.CUMULATIVE_TOTAL -> (parsedInput - previousQty).coerceAtLeast(0)
        ProductionEntryMode.DIRECT_TODAY -> parsedInput.coerceAtLeast(0)
    }
    val calculatedTotal = allOtherEntriesOnPo + calculatedToday
    val remainingQty = if (selectedPoSummary != null) {
        (selectedPoSummary.po.orderQuantity - calculatedTotal).coerceAtLeast(0)
    } else 0

    fun resetForm() {
        onCancelEdit()
        selectedDate = DateUtils.todayIso()
        selectedPoId = ""
        selectedLineId = ""
        workerName = ""
        targetQtyText = ""
        quantityInputText = ""
        errorMsg = null
    }

    fun submitSave() {
        focusManager.clearFocus()
        val err = onSaveProduction(
            editing?.entry?.id,
            selectedDate,
            selectedPoId,
            selectedLineId.ifBlank { null },
            workerName,
            targetQtyText,
            mode,
            quantityInputText
        )
        if (err != null) {
            errorMsg = err
        } else {
            resetForm()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.productionEntry,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = strings.productionSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onNavigateBalance,
                            border = BorderStroke(1.dp, NavyBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("production_balance_shortcut")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = strings.navBalance,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(strings.navBalanceShort, color = AmberWarning)
                        }
                        OutlinedButton(
                            onClick = onNavigateHistory,
                            border = BorderStroke(1.dp, NavyBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("production_history_shortcut")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = strings.navHistory,
                                tint = SkyBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(strings.navHistory, color = SkyBlue)
                        }
                    }
                }
            }

            if (uiState.purchaseOrders.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = AmberWarning.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.noPOs,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = onNavigatePOs,
                                modifier = Modifier.testTag("production_go_to_po_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.addPO)
                            }
                        }
                    }
                }
            }

            if (editing != null) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = RoyalPurple.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, RoyalPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.editingEntryBanner,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { resetForm() }) {
                                Text(strings.cancel)
                            }
                        }
                    }
                }
            }

            // Calculation Info Banner
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = BrightBlue.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (mode == ProductionEntryMode.CUMULATIVE_TOTAL) {
                                strings.productionCalcHintCumulative
                            } else {
                                strings.productionCalcHintDirect
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Form Card
            item {
                AhbCard(modifier = Modifier.fillMaxWidth()) {
                    // Mode Toggle Chips
                    Text(
                        text = strings.entryModeLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = mode == ProductionEntryMode.CUMULATIVE_TOTAL,
                            onClick = {
                                if (mode != ProductionEntryMode.CUMULATIVE_TOTAL) {
                                    mode = ProductionEntryMode.CUMULATIVE_TOTAL
                                    quantityInputText = if (calculatedToday > 0) (previousQty + calculatedToday).toString() else ""
                                }
                            },
                            label = { Text(strings.modeCumulativeTotal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("mode_cumulative_chip")
                        )
                        FilterChip(
                            selected = mode == ProductionEntryMode.DIRECT_TODAY,
                            onClick = {
                                if (mode != ProductionEntryMode.DIRECT_TODAY) {
                                    mode = ProductionEntryMode.DIRECT_TODAY
                                    quantityInputText = if (calculatedToday > 0) calculatedToday.toString() else ""
                                }
                            },
                            label = { Text(strings.modeDirectToday) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalPurple,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("mode_direct_chip")
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AhbDatePickerField(
                            label = strings.productionDate,
                            dateIso = selectedDate,
                            onDateSelected = { selectedDate = it; errorMsg = null },
                            testTag = "production_date_input"
                        )

                        AhbDropdownField(
                            label = strings.selectPO,
                            selectedLabel = selectedPoSummary?.let { "${it.po.poNumber} · ${it.buyerName}" } ?: "",
                            options = poOptions,
                            onSelect = { poId ->
                                selectedPoId = poId
                                errorMsg = null
                            },
                            testTag = "production_po_dropdown"
                        )

                        // Auto-filled Buyer Name & Style Name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AhbTextField(
                                value = selectedPoSummary?.buyerName ?: "—",
                                onValueChange = {},
                                label = strings.selectBuyer.removeSuffix(" *"),
                                readOnly = true,
                                modifier = Modifier.weight(1f),
                                testTag = "production_autofill_buyer"
                            )
                            AhbTextField(
                                value = selectedPoSummary?.styleName ?: "—",
                                onValueChange = {},
                                label = strings.selectStyle.removeSuffix(" *"),
                                readOnly = true,
                                modifier = Modifier.weight(1f),
                                testTag = "production_autofill_style"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AhbDropdownField(
                                label = strings.productionLine,
                                selectedLabel = lineOptions.firstOrNull { it.value == selectedLineId }?.label ?: strings.optionalLine,
                                options = lineOptions,
                                onSelect = { lId ->
                                    selectedLineId = lId
                                    val line = uiState.lines.firstOrNull { it.line.id == lId }?.line
                                    if (line != null && targetQtyText.isBlank() && line.dailyTarget > 0) {
                                        targetQtyText = line.dailyTarget.toString()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "production_line_dropdown"
                            )
                            AhbTextField(
                                value = targetQtyText,
                                onValueChange = { input -> targetQtyText = input.filter { it.isDigit() } },
                                label = strings.targetQuantity,
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                modifier = Modifier.weight(1f),
                                testTag = "production_target_input"
                            )
                        }

                        AhbTextField(
                            value = workerName,
                            onValueChange = { workerName = it },
                            label = strings.workerName,
                            imeAction = ImeAction.Next,
                            testTag = "production_worker_input"
                        )

                        AhbTextField(
                            value = quantityInputText,
                            onValueChange = { input ->
                                quantityInputText = input.filter { it.isDigit() }
                                errorMsg = null
                            },
                            label = if (mode == ProductionEntryMode.CUMULATIVE_TOTAL) {
                                strings.totalProductionInput
                            } else {
                                strings.todayProductionInput
                            },
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { submitSave() }),
                            testTag = "production_qty_input"
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Live Auto-Calculation Summary Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(NavySurfaceElevated)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (selectedPoSummary != null) {
                                        "${selectedPoSummary.po.poNumber} · ${strings.orderQuantity.removeSuffix(" *")}: ${DateUtils.formatNumber(selectedPoSummary.po.orderQuantity)}"
                                    } else {
                                        strings.selectPO
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SkyBlue
                                )
                            }
                            if (selectedPoSummary != null && selectedPoSummary.po.orderQuantity > 0) {
                                val pct = ((calculatedTotal.toFloat() / selectedPoSummary.po.orderQuantity.toFloat()) * 100f)
                                    .toInt()
                                    .coerceIn(0, 100)
                                Text(
                                    text = "$pct%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingQty == 0) EmeraldSuccess else AmberWarning
                                )
                            }
                        }
                        if (selectedPoSummary != null && selectedPoSummary.po.orderQuantity > 0) {
                            val liveProgress = (calculatedTotal.toFloat() / selectedPoSummary.po.orderQuantity.toFloat()).coerceIn(0f, 1f)
                            AhbProgressBar(
                                progress = liveProgress,
                                color = if (calculatedTotal > selectedPoSummary.po.orderQuantity) CoralError
                                else if (remainingQty == 0) EmeraldSuccess else BrightBlue
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CalcReadout(strings.previousProduction, DateUtils.formatNumber(previousQty), TextSecondary)
                            CalcReadout(
                                if (mode == ProductionEntryMode.CUMULATIVE_TOTAL) strings.autoTodayProduction else strings.todayProductionInput.removeSuffix(" *"),
                                DateUtils.formatNumber(calculatedToday),
                                EmeraldSuccess
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CalcReadout(
                                if (mode == ProductionEntryMode.DIRECT_TODAY) strings.autoTotalProduction else strings.totalProduction,
                                DateUtils.formatNumber(calculatedTotal),
                                BrightBlue
                            )
                            CalcReadout(
                                strings.remainingBalance,
                                DateUtils.formatNumber(remainingQty),
                                if (selectedPoSummary != null && remainingQty == 0) EmeraldSuccess else AmberWarning
                            )
                        }
                    }
                }
            }
        }

        // Sticky Always-Visible Bottom Save Bar
        Surface(
            color = NavySurface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = NavyBorder)
                AnimatedVisibility(visible = errorMsg != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = CoralError.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, CoralError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = CoralError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AhbGradientButton(
                        text = if (editing != null) strings.updateProduction else strings.saveProduction,
                        icon = Icons.Default.Save,
                        onClick = { submitSave() },
                        modifier = Modifier.weight(1f),
                        testTag = "save_production_button"
                    )
                    OutlinedButton(
                        onClick = { resetForm() },
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("reset_production_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = strings.reset,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(strings.reset, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcReadout(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
