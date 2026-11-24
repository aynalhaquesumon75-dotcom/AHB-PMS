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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbConfirmDialog
import com.example.ui.components.AhbGradientButton
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
fun BackupScreen(
    uiState: PmsUiState,
    onBuildBackupJson: suspend () -> String,
    onRestoreBackupJson: (String) -> Unit,
    onLoadSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    onNotify: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var showSampleConfirm by remember { mutableStateOf(false) }
    var showClearStep1 by remember { mutableStateOf(false) }
    var showClearStep2 by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = onBuildBackupJson()
                val res = ExportUtils.writeTextToUri(context, uri, json)
                if (res.isSuccess) {
                    onNotify(strings.msgBackupExported)
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage ?: "Backup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = ExportUtils.readTextFromUri(context, uri)
                if (res.isSuccess) {
                    pendingRestoreJson = res.getOrNull()
                } else {
                    onNotify(strings.msgInvalidBackup)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = strings.backupTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = strings.backupSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Database Status Summary
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusCount(strings.totalBuyers, uiState.buyers.size, BrightBlue)
                    StatusCount(strings.totalStyles, uiState.styles.size, SoftViolet)
                    StatusCount(strings.totalPOs, uiState.purchaseOrders.size, EmeraldSuccess)
                    StatusCount(strings.totalLines, uiState.lines.size, AmberWarning)
                }
            }
        }

        // Export JSON Backup
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.exportJsonTitle, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(6.dp))
                Text(strings.exportJsonDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                AhbGradientButton(
                    text = strings.downloadBackup,
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        createBackupLauncher.launch("AHB_PMS_Backup_${DateUtils.todayIso()}.json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "download_backup_button"
                )
            }
        }

        // Restore JSON Backup
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.restoreJsonTitle, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(6.dp))
                Text(strings.restoreJsonDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        openBackupLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restore_backup_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.selectBackupFile, color = Color.White)
                }
            }
        }

        // Optional Sample Factory Data Loader
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dataset, contentDescription = null, tint = SoftViolet, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.sampleDataTitle, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(6.dp))
                Text(strings.sampleDataDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showSampleConfirm = true },
                    border = BorderStroke(1.dp, SoftViolet),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("load_sample_data_button")
                ) {
                    Icon(Icons.Default.Dataset, contentDescription = null, tint = SoftViolet, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.loadSampleData, color = SoftViolet)
                }
            }
        }

        // Danger Zone: Clear All Data
        item {
            AhbCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CoralError, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.clearDataTitle, style = MaterialTheme.typography.titleLarge, color = CoralError)
                }
                Spacer(Modifier.height(6.dp))
                Text(strings.clearDataDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { showClearStep1 = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_all_data_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.clearAllData, color = Color.White)
                }
            }
        }
    }

    pendingRestoreJson?.let { rawJson ->
        AhbConfirmDialog(
            title = strings.restoreConfirmTitle,
            message = strings.restoreConfirmMsg,
            confirmText = strings.confirm,
            isDestructive = false,
            onConfirm = {
                onRestoreBackupJson(rawJson)
                pendingRestoreJson = null
            },
            onDismiss = { pendingRestoreJson = null }
        )
    }

    if (showSampleConfirm) {
        AhbConfirmDialog(
            title = strings.sampleConfirmTitle,
            message = strings.sampleConfirmMsg,
            confirmText = strings.confirm,
            isDestructive = false,
            onConfirm = {
                onLoadSampleData()
                showSampleConfirm = false
            },
            onDismiss = { showSampleConfirm = false }
        )
    }

    if (showClearStep1) {
        AhbConfirmDialog(
            title = strings.clearConfirmStep1Title,
            message = strings.clearConfirmStep1Msg,
            confirmText = strings.confirm,
            isDestructive = true,
            onConfirm = {
                showClearStep1 = false
                showClearStep2 = true
            },
            onDismiss = { showClearStep1 = false }
        )
    }

    if (showClearStep2) {
        AhbConfirmDialog(
            title = strings.clearConfirmStep2Title,
            message = strings.clearConfirmStep2Msg,
            confirmText = strings.clearAllData,
            isDestructive = true,
            onConfirm = {
                onClearAllData()
                showClearStep2 = false
            },
            onDismiss = { showClearStep2 = false }
        )
    }
}

@Composable
private fun StatusCount(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = DateUtils.formatNumber(count), style = MaterialTheme.typography.titleLarge, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
