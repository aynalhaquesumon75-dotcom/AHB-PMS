package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalStrings
import com.example.ui.ChartPoint
import com.example.ui.theme.ChartBarGradientBrush
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.PrimaryGradientBrush
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun AhbCard(
    modifier: Modifier = Modifier,
    containerColor: Color = NavySurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, NavyBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AhbGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    brush: Brush = PrimaryGradientBrush,
    testTag: String = "gradient_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .testTag(testTag),
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = NavySurfaceElevated
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (enabled) Modifier.background(brush) else Modifier.background(NavySurfaceElevated))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimary else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "kpi_card"
) {
    AhbCard(
        modifier = modifier.testTag(testTag),
        containerColor = NavySurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AhbBarChart(
    points: List<ChartPoint>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    barBrush: Brush = ChartBarGradientBrush
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(190.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val maxVal = (points.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)
    val labelColor = android.graphics.Color.parseColor("#94A3B8")
    val gridColor = NavyBorder

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        val bottomPad = 26.dp.toPx()
        val topPad = 14.dp.toPx()
        val chartHeight = (size.height - bottomPad - topPad).coerceAtLeast(10f)

        // Horizontal grid lines
        for (i in 0..3) {
            val y = topPad + chartHeight * (i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val count = points.size
        val slotWidth = size.width / count.coerceAtLeast(1)
        val barWidth = (slotWidth * 0.56f).coerceIn(6.dp.toPx(), 28.dp.toPx())
        val corner = CornerRadius(5.dp.toPx(), 5.dp.toPx())

        val labelPaint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val valuePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val labelStep = when {
            count > 20 -> 3
            count > 12 -> 2
            else -> 1
        }

        points.forEachIndexed { idx, point ->
            val ratio = (point.value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
            val barH = if (point.value > 0) (chartHeight * ratio).coerceAtLeast(6.dp.toPx()) else 0f
            val centerX = slotWidth * idx + slotWidth / 2f
            val left = centerX - barWidth / 2f
            val top = topPad + (chartHeight - barH)

            if (barH > 0f) {
                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barH),
                    cornerRadius = corner
                )
                if (count <= 10) {
                    drawContext.canvas.nativeCanvas.drawText(
                        DateUtils.formatNumber(point.value),
                        centerX,
                        (top - 4.dp.toPx()).coerceAtLeast(10f),
                        valuePaint
                    )
                }
            }

            if (idx % labelStep == 0 || idx == count - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    point.label,
                    centerX,
                    size.height - 4.dp.toPx(),
                    labelPaint
                )
            }
        }
    }
}

@Composable
fun AhbProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = EmeraldSuccess
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "progress_anim"
    )
    LinearProgressIndicator(
        progress = { animated },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = NavySurfaceElevated,
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun AhbTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = "text_field"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = TextMuted) }
        } else null,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = NavyBorder,
            focusedContainerColor = if (readOnly) NavySurfaceElevated.copy(alpha = 0.5f) else NavySurfaceElevated,
            unfocusedContainerColor = if (readOnly) NavySurfaceElevated.copy(alpha = 0.5f) else NavySurfaceElevated,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

data class DropdownOption<T>(
    val value: T,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AhbDropdownField(
    label: String,
    selectedLabel: String,
    options: List<DropdownOption<T>>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "dropdown_field"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = NavyBorder,
                focusedContainerColor = NavySurfaceElevated,
                unfocusedContainerColor = NavySurfaceElevated,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = TextSecondary
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .testTag(testTag)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = NavySurfaceElevated
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("—", color = TextMuted) },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onSelect(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AhbDatePickerField(
    label: String,
    dateIso: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "date_picker_field"
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        AhbTextField(
            value = dateIso,
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            testTag = testTag
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .minimumInteractiveComponentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        val initialMillis = DateUtils.isoDateToMillis(dateIso) ?: System.currentTimeMillis()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        val strings = LocalStrings.current
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateUtils.millisToIsoDate(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun AhbConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = true
) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurfaceElevated,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) CoralError else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text(strings.cancel, color = TextSecondary)
            }
        }
    )
}

@Composable
fun AhbEmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    AhbCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                AhbGradientButton(
                    text = actionLabel,
                    onClick = onAction,
                    testTag = "empty_state_action"
                )
            }
        }
    }
}

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(50),
        color = NavySurfaceElevated,
        border = BorderStroke(1.dp, NavyBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("language_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Switch Language",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (language == AppLanguage.EN) "EN · বাংলা" else "বাংলা · EN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
