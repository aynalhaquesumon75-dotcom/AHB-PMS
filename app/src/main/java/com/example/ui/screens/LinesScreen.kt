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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.ViewModule
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.ChartPoint
import com.example.ui.PmsUiState
import com.example.ui.ProductionLineSummary
import com.example.ui.components.AhbBarChart
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbEmptyState
import com.example.ui.components.AhbGradientButton
import com.example.ui.components.AhbProgressBar
import com.example.ui.components.AhbTextField
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
fun LinesScreen(
    uiState: PmsUiState,
    onSaveLine: (editId: String?, name: String, supervisor: String, dailyTarget: String, workerCount: String) -> String?,
    onDeleteLine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var showEditor by remember { mutableStateOf(false) }
    var editingLine by remember { mutableStateOf<ProductionLineSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<ProductionLineSummary?>(null) }

    val lineChartPoints = remember(uiState.lines) {
        uiState.lines.map { ChartPoint(it.line.lineName.take(10), it.todayProduction) }
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
                        text = strings.lineManagement,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = strings.lineSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            if (uiState.lines.isNotEmpty()) {
                item {
                    AhbCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.lineWiseSummary,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        AhbBarChart(
                            points = lineChartPoints,
                            emptyMessage = strings.noChartData
                        )
                    }
                }
            }

            if (uiState.lines.isEmpty()) {
                item {
                    AhbEmptyState(
                        icon = Icons.Default.ViewModule,
                        message = strings.noLines,
                        actionLabel = strings.addLine,
                        onAction = {
                            editingLine = null
                            showEditor = true
                        }
                    )
                }
            } else {
                items(uiState.lines, key = { it.line.id }) { item ->
                    val pct = (item.targetProgressFraction * 100).toInt()
                    AhbCard(modifier = Modifier.fillMaxWidth().testTag("line_card_${item.line.id}")) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.line.lineName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (item.line.supervisorName.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "${strings.lineSupervisor}: ${item.line.supervisorName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${strings.workerCount}: ${DateUtils.formatNumber(item.line.workerCount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = {
                                        editingLine = item
                                        showEditor = true
                                    },
                                    modifier = Modifier.testTag("line_edit_btn")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = SoftViolet)
                                }
                                IconButton(
                                    onClick = { deleteTarget = item },
                                    modifier = Modifier.testTag("line_delete_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = CoralError)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.todayProduction}: ${DateUtils.formatNumber(item.todayProduction)} / ${DateUtils.formatNumber(item.line.dailyTarget)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SkyBlue
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 100) EmeraldSuccess else AmberWarning
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        AhbProgressBar(
                            progress = item.targetProgressFraction,
                            color = if (pct >= 100) EmeraldSuccess else BrightBlue
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrightBlue.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${strings.totalProduction}: ${DateUtils.formatNumber(item.totalProduction)} (${item.entryCount} ${strings.reportEntriesCount})",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrightBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingLine = null
                showEditor = true
            },
            containerColor = ElectricBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_line_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.addLine)
        }
    }

    if (showEditor) {
        LineEditorDialog(
            initial = editingLine,
            onDismiss = { showEditor = false },
            onSave = { name, supervisor, target, workers ->
                val err = onSaveLine(editingLine?.line?.id, name, supervisor, target, workers)
                if (err == null) showEditor = false
                err
            }
        )
    }

    deleteTarget?.let { target ->
        AhbConfirmDialog(
            title = strings.deleteLineTitle,
            message = "${strings.deleteLineConfirm}\n(${target.line.lineName})",
            confirmText = strings.delete,
            onConfirm = {
                onDeleteLine(target.line.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun LineEditorDialog(
    initial: ProductionLineSummary?,
    onDismiss: () -> Unit,
    onSave: (name: String, supervisor: String, dailyTarget: String, workerCount: String) -> String?
) {
    val strings = LocalStrings.current
    var lineName by remember { mutableStateOf(initial?.line?.lineName ?: "") }
    var supervisor by remember { mutableStateOf(initial?.line?.supervisorName ?: "") }
    var dailyTarget by remember { mutableStateOf(initial?.line?.dailyTarget?.takeIf { it > 0 }?.toString() ?: "") }
    var workerCount by remember { mutableStateOf(initial?.line?.workerCount?.takeIf { it > 0 }?.toString() ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = if (initial == null) strings.addLine else strings.editLine,
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
                    value = lineName,
                    onValueChange = { lineName = it; errorMsg = null },
                    label = strings.lineName,
                    testTag = "line_name_input"
                )
                AhbTextField(
                    value = supervisor,
                    onValueChange = { supervisor = it },
                    label = strings.lineSupervisor,
                    testTag = "line_supervisor_input"
                )
                AhbTextField(
                    value = dailyTarget,
                    onValueChange = { input -> dailyTarget = input.filter { it.isDigit() }; errorMsg = null },
                    label = strings.dailyTarget,
                    keyboardType = KeyboardType.Number,
                    testTag = "line_target_input"
                )
                AhbTextField(
                    value = workerCount,
                    onValueChange = { input -> workerCount = input.filter { it.isDigit() }; errorMsg = null },
                    label = strings.workerCount,
                    keyboardType = KeyboardType.Number,
                    testTag = "line_worker_count_input"
                )
            }
        },
        confirmButton = {
            AhbGradientButton(
                text = if (initial == null) strings.save else strings.update,
                onClick = {
                    val err = onSave(lineName, supervisor, dailyTarget, workerCount)
                    if (err != null) errorMsg = err
                },
                testTag = "save_line_button"
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
