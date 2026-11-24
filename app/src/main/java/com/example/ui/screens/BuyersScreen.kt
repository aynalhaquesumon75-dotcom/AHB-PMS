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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.ui.BuyerSummary
import com.example.ui.PmsUiState
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbTextField
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

private enum class BuyerFilter { ALL, HAS_ORDERS, HAS_STYLES }

@Composable
fun BuyersScreen(
    uiState: PmsUiState,
    onSaveBuyer: (editId: String?, name: String, contact: String, phone: String, email: String, address: String) -> String?,
    onDeleteBuyer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(BuyerFilter.ALL) }

    var showEditor by remember { mutableStateOf(false) }
    var editingBuyer by remember { mutableStateOf<BuyerSummary?>(null) }
    var detailsBuyer by remember { mutableStateOf<BuyerSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<BuyerSummary?>(null) }

    val filtered = remember(uiState.buyers, searchQuery, filter) {
        val q = searchQuery.trim().lowercase()
        uiState.buyers.filter { item ->
            val matchesQuery = q.isEmpty() ||
                item.buyer.name.lowercase().contains(q) ||
                item.buyer.contactPerson.lowercase().contains(q) ||
                item.buyer.phone.lowercase().contains(q) ||
                item.buyer.email.lowercase().contains(q)
            val matchesFilter = when (filter) {
                BuyerFilter.ALL -> true
                BuyerFilter.HAS_ORDERS -> item.poCount > 0
                BuyerFilter.HAS_STYLES -> item.styleCount > 0
            }
            matchesQuery && matchesFilter
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
                        text = strings.buyerManagement,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.buyerSubtitle,
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
                        label = strings.searchBuyers,
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = strings.search, tint = TextSecondary)
                        },
                        testTag = "buyer_search_input"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filter == BuyerFilter.ALL,
                            onClick = { filter = BuyerFilter.ALL },
                            label = { Text(strings.filterAllBuyers) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("buyer_filter_all")
                        )
                        FilterChip(
                            selected = filter == BuyerFilter.HAS_ORDERS,
                            onClick = { filter = BuyerFilter.HAS_ORDERS },
                            label = { Text(strings.filterHasOrders) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("buyer_filter_orders")
                        )
                        FilterChip(
                            selected = filter == BuyerFilter.HAS_STYLES,
                            onClick = { filter = BuyerFilter.HAS_STYLES },
                            label = { Text(strings.filterHasStyles) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("buyer_filter_styles")
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    AhbEmptyState(
                        icon = Icons.Default.Business,
                        message = strings.noBuyers,
                        actionLabel = strings.addBuyer,
                        onAction = {
                            editingBuyer = null
                            showEditor = true
                        }
                    )
                }
            } else {
                items(filtered, key = { it.buyer.id }) { item ->
                    BuyerCard(
                        item = item,
                        onViewDetails = { detailsBuyer = item },
                        onEdit = {
                            editingBuyer = item
                            showEditor = true
                        },
                        onDelete = { deleteTarget = item }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingBuyer = null
                showEditor = true
            },
            containerColor = ElectricBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_buyer_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addBuyer)
        }
    }

    if (showEditor) {
        BuyerEditorDialog(
            initial = editingBuyer,
            onDismiss = { showEditor = false },
            onSave = { name, contact, phone, email, address ->
                val err = onSaveBuyer(editingBuyer?.buyer?.id, name, contact, phone, email, address)
                if (err == null) showEditor = false
                err
            }
        )
    }

    detailsBuyer?.let { item ->
        BuyerDetailsDialog(
            item = item,
            uiState = uiState,
            onDismiss = { detailsBuyer = null }
        )
    }

    deleteTarget?.let { target ->
        AhbConfirmDialog(
            title = strings.deleteBuyerTitle,
            message = "${strings.deleteBuyerConfirm}\n(${target.buyer.name})",
            confirmText = strings.delete,
            onConfirm = {
                onDeleteBuyer(target.buyer.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun BuyerCard(
    item: BuyerSummary,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalStrings.current
    AhbCard(modifier = Modifier.fillMaxWidth().testTag("buyer_card_${item.buyer.id}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.buyer.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (item.buyer.contactPerson.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(item.buyer.contactPerson, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                if (item.buyer.phone.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(item.buyer.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            Row {
                IconButton(onClick = onViewDetails, modifier = Modifier.testTag("buyer_details_btn")) {
                    Icon(Icons.Default.Info, contentDescription = strings.details, tint = BrightBlue)
                }
                IconButton(onClick = onEdit, modifier = Modifier.testTag("buyer_edit_btn")) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = SoftViolet)
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("buyer_delete_btn")) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = CoralError)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBadge("${strings.navStyles}: ${item.styleCount}", BrightBlue)
            StatBadge("${strings.navPurchaseOrders}: ${item.poCount}", SoftViolet)
            StatBadge("${strings.orderQuantity.removeSuffix(" *")}: ${DateUtils.formatNumber(item.totalOrderQty)}", EmeraldSuccess)
        }
    }
}

@Composable
private fun StatBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BuyerEditorDialog(
    initial: BuyerSummary?,
    onDismiss: () -> Unit,
    onSave: (name: String, contact: String, phone: String, email: String, address: String) -> String?
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(initial?.buyer?.name ?: "") }
    var contact by remember { mutableStateOf(initial?.buyer?.contactPerson ?: "") }
    var phone by remember { mutableStateOf(initial?.buyer?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.buyer?.email ?: "") }
    var address by remember { mutableStateOf(initial?.buyer?.address ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = if (initial == null) strings.addBuyer else strings.editBuyer,
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
                AhbTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = strings.buyerName,
                    testTag = "buyer_name_input"
                )
                AhbTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = strings.contactPerson,
                    testTag = "buyer_contact_input"
                )
                AhbTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = strings.phoneNumber,
                    keyboardType = KeyboardType.Phone,
                    testTag = "buyer_phone_input"
                )
                AhbTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = strings.emailAddress,
                    keyboardType = KeyboardType.Email,
                    testTag = "buyer_email_input"
                )
                AhbTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = strings.address,
                    singleLine = false,
                    maxLines = 3,
                    testTag = "buyer_address_input"
                )
            }
        },
        confirmButton = {
            AhbGradientButton(
                text = if (initial == null) strings.save else strings.update,
                onClick = {
                    val err = onSave(name, contact, phone, email, address)
                    if (err != null) errorMsg = err
                },
                testTag = "save_buyer_button"
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
private fun BuyerDetailsDialog(
    item: BuyerSummary,
    uiState: PmsUiState,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val styles = uiState.styles.filter { it.style.buyerId == item.buyer.id }
    val pos = uiState.purchaseOrders.filter { it.po.buyerId == item.buyer.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(text = "${strings.buyerDetails}: ${item.buyer.name}", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.buyer.contactPerson.isNotBlank()) {
                    DetailRow(Icons.Default.Person, item.buyer.contactPerson)
                }
                if (item.buyer.phone.isNotBlank()) {
                    DetailRow(Icons.Default.Phone, item.buyer.phone)
                }
                if (item.buyer.email.isNotBlank()) {
                    DetailRow(Icons.Default.Email, item.buyer.email)
                }
                if (item.buyer.address.isNotBlank()) {
                    DetailRow(Icons.Default.LocationOn, item.buyer.address)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${strings.linkedStyles} (${styles.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = BrightBlue
                )
                if (styles.isEmpty()) {
                    Text("—", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } else {
                    styles.forEach { s ->
                        Text(
                            text = "• ${s.style.styleName}" + if (s.style.styleNumber.isNotBlank()) " (${s.style.styleNumber})" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${strings.linkedPOs} (${pos.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = SoftViolet
                )
                if (pos.isEmpty()) {
                    Text("—", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } else {
                    pos.forEach { p ->
                        Text(
                            text = "• ${p.po.poNumber}: ${DateUtils.formatNumber(p.totalProduced)} / ${DateUtils.formatNumber(p.po.orderQuantity)} ${p.po.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
