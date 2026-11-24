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
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.StyleSummary
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbDropdownField
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbTextField
import com.example.ui.components.DropdownOption
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun StylesScreen(
    uiState: PmsUiState,
    onSaveStyle: (editId: String?, buyerId: String, styleName: String, styleNumber: String, description: String) -> String?,
    onDeleteStyle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBuyerFilter by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingStyle by remember { mutableStateOf<StyleSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<StyleSummary?>(null) }

    val filtered = remember(uiState.styles, searchQuery, selectedBuyerFilter) {
        val q = searchQuery.trim().lowercase()
        uiState.styles.filter { item ->
            val matchesBuyer = selectedBuyerFilter.isBlank() || item.style.buyerId == selectedBuyerFilter
            val matchesQuery = q.isEmpty() ||
                item.style.styleName.lowercase().contains(q) ||
                item.style.styleNumber.lowercase().contains(q) ||
                item.buyerName.lowercase().contains(q)
            matchesBuyer && matchesQuery
        }
    }

    val buyerFilterOptions = remember(uiState.buyers, strings.all) {
        listOf(DropdownOption("", strings.all)) +
            uiState.buyers.map { DropdownOption(it.buyer.id, it.buyer.name) }
    }
    val selectedFilterLabel = buyerFilterOptions.firstOrNull { it.value == selectedBuyerFilter }?.label ?: strings.all

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = strings.styleManagement,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.styleSubtitle,
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
                        label = strings.searchStyles,
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = strings.search, tint = TextSecondary)
                        },
                        testTag = "style_search_input"
                    )
                    AhbDropdownField(
                        label = strings.filterByBuyer,
                        selectedLabel = selectedFilterLabel,
                        options = buyerFilterOptions,
                        onSelect = { selectedBuyerFilter = it },
                        testTag = "style_buyer_filter"
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    AhbEmptyState(
                        icon = Icons.Default.Checkroom,
                        message = strings.noStyles,
                        actionLabel = if (uiState.buyers.isNotEmpty()) strings.addStyle else null,
                        onAction = {
                            editingStyle = null
                            showEditor = true
                        }
                    )
                }
            } else {
                items(filtered, key = { it.style.id }) { item ->
                    AhbCard(modifier = Modifier.fillMaxWidth().testTag("style_card_${item.style.id}")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.style.styleName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.style.styleNumber.isNotBlank()) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = BrightBlue.copy(alpha = 0.16f)
                                        ) {
                                            Text(
                                                text = item.style.styleNumber,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrightBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.buyerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftViolet
                                )
                                if (item.style.description.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.style.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        editingStyle = item
                                        showEditor = true
                                    },
                                    modifier = Modifier.testTag("style_edit_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = SoftViolet)
                                }
                                IconButton(
                                    onClick = { deleteTarget = item },
                                    modifier = Modifier.testTag("style_delete_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = CoralError)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${strings.navPurchaseOrders}: ${item.poCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrightBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${strings.producedQty}: ${DateUtils.formatNumber(item.totalProducedQty)} / ${DateUtils.formatNumber(item.totalOrderQty)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrightBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingStyle = null
                showEditor = true
            },
            containerColor = ElectricBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_style_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addStyle)
        }
    }

    if (showEditor) {
        StyleEditorDialog(
            initial = editingStyle,
            uiState = uiState,
            onDismiss = { showEditor = false },
            onSave = { buyerId, name, number, desc ->
                val err = onSaveStyle(editingStyle?.style?.id, buyerId, name, number, desc)
                if (err == null) showEditor = false
                err
            }
        )
    }

    deleteTarget?.let { target ->
        AhbConfirmDialog(
            title = strings.deleteStyleTitle,
            message = "${strings.deleteStyleConfirm}\n(${target.style.styleName})",
            confirmText = strings.delete,
            onConfirm = {
                onDeleteStyle(target.style.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun StyleEditorDialog(
    initial: StyleSummary?,
    uiState: PmsUiState,
    onDismiss: () -> Unit,
    onSave: (buyerId: String, name: String, number: String, desc: String) -> String?
) {
    val strings = LocalStrings.current
    var buyerId by remember { mutableStateOf(initial?.style?.buyerId ?: uiState.buyers.firstOrNull()?.buyer?.id ?: "") }
    var styleName by remember { mutableStateOf(initial?.style?.styleName ?: "") }
    var styleNumber by remember { mutableStateOf(initial?.style?.styleNumber ?: "") }
    var description by remember { mutableStateOf(initial?.style?.description ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val buyerOptions = uiState.buyers.map { DropdownOption(it.buyer.id, it.buyer.name) }
    val selectedBuyerLabel = buyerOptions.firstOrNull { it.value == buyerId }?.label ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = if (initial == null) strings.addStyle else strings.editStyle,
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
                    selectedLabel = selectedBuyerLabel,
                    options = buyerOptions,
                    onSelect = { buyerId = it; errorMsg = null },
                    testTag = "style_buyer_dropdown"
                )
                AhbTextField(
                    value = styleName,
                    onValueChange = { styleName = it; errorMsg = null },
                    label = strings.styleName,
                    testTag = "style_name_input"
                )
                AhbTextField(
                    value = styleNumber,
                    onValueChange = { styleNumber = it },
                    label = strings.styleNumber,
                    testTag = "style_number_input"
                )
                AhbTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = strings.description,
                    singleLine = false,
                    maxLines = 3,
                    testTag = "style_desc_input"
                )
            }
        },
        confirmButton = {
            AhbGradientButton(
                text = if (initial == null) strings.save else strings.update,
                onClick = {
                    val err = onSave(buyerId, styleName, styleNumber, description)
                    if (err != null) errorMsg = err
                },
                testTag = "save_style_button"
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
