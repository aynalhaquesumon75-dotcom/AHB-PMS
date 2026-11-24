package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.ui.PmsUiState
import com.example.ui.components.AhbBarChart
import com.example.ui.components.AhbCard
import com.example.ui.components.AhbProgressBar
import com.example.ui.components.DropdownOption
import com.example.ui.components.AhbDropdownField
import com.example.ui.components.KpiMetricCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HeroBannerBrush
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    uiState: PmsUiState,
    onSelectMonth: (String) -> Unit,
    onNavigateBuyers: () -> Unit,
    onNavigateStyles: () -> Unit,
    onNavigatePOs: () -> Unit,
    onNavigateProduction: () -> Unit,
    onNavigateBalance: () -> Unit,
    onNavigateLines: () -> Unit,
    onNavigateHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val stats = uiState.dashboardStats

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp
        val isMedium = maxWidth >= 600.dp
        val kpiColumns = when {
            maxWidth >= 1000.dp -> 3
            isMedium -> 3
            else -> 2
        }

        val kpiItems = listOf(
            Triple(strings.totalBuyers, DateUtils.formatNumber(stats.totalBuyers), Icons.Default.People to BrightBlue),
            Triple(strings.totalStyles, DateUtils.formatNumber(stats.totalStyles), Icons.Default.Checkroom to SoftViolet),
            Triple(strings.totalPOs, DateUtils.formatNumber(stats.totalPOs), Icons.Default.ReceiptLong to EmeraldSuccess),
            Triple(strings.totalOrderQty, DateUtils.formatNumber(stats.totalOrderQty), Icons.Default.ShoppingBag to RoyalPurple),
            Triple(strings.totalProduction, DateUtils.formatNumber(stats.totalProduction), Icons.Default.PrecisionManufacturing to CyanAccent),
            Triple(strings.todayProduction, DateUtils.formatNumber(stats.todayProduction), Icons.Default.Today to AmberWarning),
            Triple(strings.remainingPOQty, DateUtils.formatNumber(stats.remainingPOQty), Icons.Default.Inventory2 to SkyBlue),
            Triple(strings.totalLines, DateUtils.formatNumber(stats.totalLines), Icons.Default.ViewModule to BrightBlue),
            Triple(strings.totalWorkers, DateUtils.formatNumber(stats.totalWorkers), Icons.Default.Groups to EmeraldSuccess)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(HeroBannerBrush)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "✦ ${strings.welcomeBadge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = strings.welcomeTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = strings.welcomeSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.86f)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Factory,
                                contentDescription = strings.appName,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.quickActions,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            QuickActionChip(
                                label = strings.addBuyer,
                                icon = Icons.Default.AddBusiness,
                                brush = Brush.linearGradient(listOf(ElectricBlue, BrightBlue)),
                                onClick = onNavigateBuyers,
                                testTag = "quick_add_buyer"
                            )
                        }
                        item {
                            QuickActionChip(
                                label = strings.addStyle,
                                icon = Icons.Default.Checkroom,
                                brush = Brush.linearGradient(listOf(Color(0xFF059669), EmeraldSuccess)),
                                onClick = onNavigateStyles,
                                testTag = "quick_add_style"
                            )
                        }
                        item {
                            QuickActionChip(
                                label = strings.addPO,
                                icon = Icons.Default.Assignment,
                                brush = Brush.linearGradient(listOf(RoyalPurple, SoftViolet)),
                                onClick = onNavigatePOs,
                                testTag = "quick_add_po"
                            )
                        }
                        item {
                            QuickActionChip(
                                label = strings.productionEntry,
                                icon = Icons.Default.PrecisionManufacturing,
                                brush = Brush.linearGradient(listOf(Color(0xFFD97706), AmberWarning)),
                                onClick = onNavigateProduction,
                                testTag = "quick_new_production"
                            )
                        }
                        item {
                            QuickActionChip(
                                label = strings.navBalance,
                                icon = Icons.Default.Inventory2,
                                brush = Brush.linearGradient(listOf(Color(0xFF2563EB), SkyBlue)),
                                onClick = onNavigateBalance,
                                testTag = "quick_production_balance"
                            )
                        }
                        item {
                            QuickActionChip(
                                label = strings.addLine,
                                icon = Icons.Default.ViewModule,
                                brush = Brush.linearGradient(listOf(Color(0xFF0891B2), CyanAccent)),
                                onClick = onNavigateLines,
                                testTag = "quick_add_line"
                            )
                        }
                    }
                }
            }

            // 9 KPI Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val rows = kpiItems.chunked(kpiColumns)
                    rows.forEachIndexed { rIdx, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEachIndexed { cIdx, (label, value, iconPair) ->
                                KpiMetricCard(
                                    label = label,
                                    value = value,
                                    icon = iconPair.first,
                                    accentColor = iconPair.second,
                                    modifier = Modifier.weight(1f),
                                    testTag = "kpi_card_${rIdx}_$cIdx"
                                )
                            }
                            repeat(kpiColumns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Charts Section (Side-by-side on tablets, stacked on phones)
            item {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MonthlyOverviewChartCard(
                            uiState = uiState,
                            onSelectMonth = onSelectMonth,
                            modifier = Modifier.weight(1.3f)
                        )
                        DailyProductionChartCard(
                            uiState = uiState,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MonthlyOverviewChartCard(
                            uiState = uiState,
                            onSelectMonth = onSelectMonth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DailyProductionChartCard(
                            uiState = uiState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // PO Completion Progress + Recent Activities
            item {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PoCompletionProgressCard(
                            uiState = uiState,
                            onViewAll = onNavigatePOs,
                            modifier = Modifier.weight(1f)
                        )
                        RecentActivitiesCard(
                            uiState = uiState,
                            onViewAll = onNavigateHistory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PoCompletionProgressCard(
                            uiState = uiState,
                            onViewAll = onNavigatePOs,
                            modifier = Modifier.fillMaxWidth()
                        )
                        RecentActivitiesCard(
                            uiState = uiState,
                            onViewAll = onNavigateHistory,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    brush: Brush,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier
            .height(48.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .background(brush)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MonthlyOverviewChartCard(
    uiState: PmsUiState,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    AhbCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = BrightBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = strings.productionOverview,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            AhbDropdownField(
                label = strings.selectMonth,
                selectedLabel = DateUtils.formatMonthTitle(uiState.selectedDashboardMonth),
                options = uiState.availableMonths.map { DropdownOption(it, DateUtils.formatMonthTitle(it)) },
                onSelect = onSelectMonth,
                modifier = Modifier.width(155.dp),
                testTag = "dashboard_month_dropdown"
            )
        }
        Spacer(Modifier.height(8.dp))
        AhbBarChart(
            points = uiState.monthlyOverviewChart,
            emptyMessage = strings.noChartData
        )
    }
}

@Composable
private fun DailyProductionChartCard(
    uiState: PmsUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    AhbCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = SoftViolet,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = strings.dailyProductionChart,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(12.dp))
        AhbBarChart(
            points = uiState.dailyLineChart,
            emptyMessage = strings.noChartData,
            barBrush = Brush.verticalGradient(listOf(SoftViolet, RoyalPurple, ElectricBlue))
        )
    }
}

@Composable
private fun PoCompletionProgressCard(
    uiState: PmsUiState,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val topPOs = uiState.purchaseOrders.take(5)

    AhbCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.poCompletionProgress,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onViewAll) {
                Text(strings.viewAll)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (topPOs.isEmpty()) {
            Text(
                text = strings.noPoProgress,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topPOs.forEach { item ->
                    val pct = (item.progressFraction * 100).toInt()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(NavySurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.po.poNumber} · ${item.buyerName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (pct >= 100) EmeraldSuccess else SkyBlue
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${item.styleName} · ${DateUtils.formatNumber(item.totalProduced)} / ${DateUtils.formatNumber(item.po.orderQuantity)} ${item.po.unit} · ${strings.poBalanceShort}: ${DateUtils.formatNumber(item.remainingQty)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        AhbProgressBar(
                            progress = item.progressFraction,
                            color = if (pct >= 100) EmeraldSuccess else BrightBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivitiesCard(
    uiState: PmsUiState,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val recent = uiState.productionEntries.take(6)

    AhbCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.recentActivities,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onViewAll) {
                Text(strings.viewAll)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        if (recent.isEmpty()) {
            Text(
                text = strings.noRecentEntries,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recent.forEach { item ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = NavySurfaceElevated,
                        border = BorderStroke(1.dp, NavyBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.poNumber} · ${item.buyerName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append(item.entry.productionDate)
                                        append(" · ")
                                        append(item.styleName)
                                        if (item.lineName.isNotBlank()) {
                                            append(" · ")
                                            append(item.lineName)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldSuccess.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "+${DateUtils.formatNumber(item.entry.todayQuantity)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
