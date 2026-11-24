package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BuyerEntity
import com.example.data.local.ProductionEntryEntity
import com.example.data.local.ProductionLineEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.StyleEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.PmsRepository
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.AppStringsBn
import com.example.i18n.AppStringsEn
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductionEntryMode {
    CUMULATIVE_TOTAL,
    DIRECT_TODAY
}

enum class ReportType {
    DAILY,
    MONTHLY,
    PO_WISE,
    BUYER_WISE,
    STYLE_WISE,
    LINE_WISE,
    WORKER_WISE
}

enum class ReportSort {
    QTY_DESC,
    QTY_ASC,
    NAME_ASC
}

data class BuyerSummary(
    val buyer: BuyerEntity,
    val styleCount: Int,
    val poCount: Int,
    val totalOrderQty: Int,
    val totalProducedQty: Int
) {
    val balanceQty: Int get() = (totalOrderQty - totalProducedQty).coerceAtLeast(0)
}

data class StyleSummary(
    val style: StyleEntity,
    val buyerName: String,
    val poCount: Int,
    val totalOrderQty: Int,
    val totalProducedQty: Int
) {
    val balanceQty: Int get() = (totalOrderQty - totalProducedQty).coerceAtLeast(0)
}

data class PurchaseOrderSummary(
    val po: PurchaseOrderEntity,
    val buyerName: String,
    val styleName: String,
    val styleNumber: String,
    val totalProduced: Int,
    val todayProduced: Int,
    val remainingQty: Int,
    val progressFraction: Float
)

data class ProductionLineSummary(
    val line: ProductionLineEntity,
    val todayProduction: Int,
    val totalProduction: Int,
    val entryCount: Int,
    val targetProgressFraction: Float
)

data class ProductionEntryDetail(
    val entry: ProductionEntryEntity,
    val buyerId: String,
    val buyerName: String,
    val styleId: String,
    val styleName: String,
    val poNumber: String,
    val poOrderQty: Int,
    val lineName: String
)

data class ChartPoint(
    val label: String,
    val value: Int
)

data class DashboardStats(
    val totalBuyers: Int = 0,
    val totalStyles: Int = 0,
    val totalPOs: Int = 0,
    val totalOrderQty: Int = 0,
    val totalProduction: Int = 0,
    val todayProduction: Int = 0,
    val remainingPOQty: Int = 0,
    val totalLines: Int = 0,
    val totalWorkers: Int = 0
)

data class ReportRow(
    val primaryLabel: String,
    val secondaryLabel: String,
    val tertiaryLabel: String,
    val entriesCount: Int,
    val quantity: Int,
    val targetOrOrderQty: Int = 0,
    val remainingQty: Int = 0
)

data class PmsUiState(
    val language: AppLanguage = AppLanguage.EN,
    val buyers: List<BuyerSummary> = emptyList(),
    val styles: List<StyleSummary> = emptyList(),
    val purchaseOrders: List<PurchaseOrderSummary> = emptyList(),
    val lines: List<ProductionLineSummary> = emptyList(),
    val productionEntries: List<ProductionEntryDetail> = emptyList(),
    val dashboardStats: DashboardStats = DashboardStats(),
    val availableMonths: List<String> = listOf(DateUtils.currentMonthIso()),
    val selectedDashboardMonth: String = DateUtils.currentMonthIso(),
    val monthlyOverviewChart: List<ChartPoint> = emptyList(),
    val dailyLineChart: List<ChartPoint> = emptyList(),
    val editingEntry: ProductionEntryDetail? = null,
    val preselectedPoId: String? = null
)

class PmsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).pmsDao()
    private val repository = PmsRepository(dao)
    private val prefs = PreferencesManager(application)

    private val _selectedDashboardMonth = MutableStateFlow(DateUtils.currentMonthIso())
    private val _editingEntryId = MutableStateFlow<String?>(null)
    private val _preselectedPoId = MutableStateFlow<String?>(null)

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    val uiState: StateFlow<PmsUiState> = combine(
        prefs.languageFlow,
        repository.buyers,
        repository.styles,
        repository.lines,
        repository.purchaseOrders,
        repository.productionEntries,
        _selectedDashboardMonth,
        _editingEntryId,
        _preselectedPoId
    ) { values ->
        val language = values[0] as AppLanguage
        @Suppress("UNCHECKED_CAST")
        val buyersRaw = values[1] as List<BuyerEntity>
        @Suppress("UNCHECKED_CAST")
        val stylesRaw = values[2] as List<StyleEntity>
        @Suppress("UNCHECKED_CAST")
        val linesRaw = values[3] as List<ProductionLineEntity>
        @Suppress("UNCHECKED_CAST")
        val posRaw = values[4] as List<PurchaseOrderEntity>
        @Suppress("UNCHECKED_CAST")
        val entriesRaw = values[5] as List<ProductionEntryEntity>
        val selectedMonth = values[6] as String
        val editingId = values[7] as String?
        val preselectedPo = values[8] as String?

        val today = DateUtils.todayIso()
        val buyerMap = buyersRaw.associateBy { it.id }
        val styleMap = stylesRaw.associateBy { it.id }
        val lineMap = linesRaw.associateBy { it.id }
        val poMap = posRaw.associateBy { it.id }

        val entriesByPo = entriesRaw.groupBy { it.poId }
        val entriesByLine = entriesRaw.filter { !it.lineId.isNullOrBlank() }.groupBy { it.lineId!! }

        val poSummaries = posRaw.map { po ->
            val poEntries = entriesByPo[po.id].orEmpty()
            val produced = poEntries.sumOf { it.todayQuantity }
            val todayProduced = poEntries.filter { it.productionDate == today }.sumOf { it.todayQuantity }
            val remaining = (po.orderQuantity - produced).coerceAtLeast(0)
            val fraction = if (po.orderQuantity > 0) (produced.toFloat() / po.orderQuantity.toFloat()).coerceIn(0f, 1f) else 0f
            val style = styleMap[po.styleId]
            PurchaseOrderSummary(
                po = po,
                buyerName = buyerMap[po.buyerId]?.name ?: "—",
                styleName = style?.styleName ?: "—",
                styleNumber = style?.styleNumber ?: "",
                totalProduced = produced,
                todayProduced = todayProduced,
                remainingQty = remaining,
                progressFraction = fraction
            )
        }
        val poSummaryMap = poSummaries.associateBy { it.po.id }

        val buyerSummaries = buyersRaw.map { b ->
            val buyerStyles = stylesRaw.filter { it.buyerId == b.id }
            val buyerPos = poSummaries.filter { it.po.buyerId == b.id }
            BuyerSummary(
                buyer = b,
                styleCount = buyerStyles.size,
                poCount = buyerPos.size,
                totalOrderQty = buyerPos.sumOf { it.po.orderQuantity },
                totalProducedQty = buyerPos.sumOf { it.totalProduced }
            )
        }

        val styleSummaries = stylesRaw.map { s ->
            val stylePos = poSummaries.filter { it.po.styleId == s.id }
            StyleSummary(
                style = s,
                buyerName = buyerMap[s.buyerId]?.name ?: "—",
                poCount = stylePos.size,
                totalOrderQty = stylePos.sumOf { it.po.orderQuantity },
                totalProducedQty = stylePos.sumOf { it.totalProduced }
            )
        }

        val lineSummaries = linesRaw.map { l ->
            val lineEntries = entriesByLine[l.id].orEmpty()
            val todayOut = lineEntries.filter { it.productionDate == today }.sumOf { it.todayQuantity }
            val totalOut = lineEntries.sumOf { it.todayQuantity }
            val fraction = if (l.dailyTarget > 0) (todayOut.toFloat() / l.dailyTarget.toFloat()).coerceIn(0f, 1.5f) else 0f
            ProductionLineSummary(
                line = l,
                todayProduction = todayOut,
                totalProduction = totalOut,
                entryCount = lineEntries.size,
                targetProgressFraction = fraction
            )
        }

        val entryDetails = entriesRaw.map { e ->
            val po = poMap[e.poId]
            val buyerName = po?.let { buyerMap[it.buyerId]?.name } ?: "—"
            val styleName = po?.let { styleMap[it.styleId]?.styleName } ?: "—"
            val lineName = e.lineId?.let { lineMap[it]?.lineName } ?: ""
            ProductionEntryDetail(
                entry = e,
                buyerId = po?.buyerId ?: "",
                buyerName = buyerName,
                styleId = po?.styleId ?: "",
                styleName = styleName,
                poNumber = po?.poNumber ?: "—",
                poOrderQty = po?.orderQuantity ?: 0,
                lineName = lineName
            )
        }

        val totalOrderQty = posRaw.sumOf { it.orderQuantity }
        val totalProducedAll = entriesRaw.sumOf { it.todayQuantity }
        val todayProducedAll = entriesRaw.filter { it.productionDate == today }.sumOf { it.todayQuantity }
        val remainingAll = poSummaries.sumOf { it.remainingQty }
        val totalWorkers = linesRaw.sumOf { it.workerCount }

        val stats = DashboardStats(
            totalBuyers = buyersRaw.size,
            totalStyles = stylesRaw.size,
            totalPOs = posRaw.size,
            totalOrderQty = totalOrderQty,
            totalProduction = totalProducedAll,
            todayProduction = todayProducedAll,
            remainingPOQty = remainingAll,
            totalLines = linesRaw.size,
            totalWorkers = totalWorkers
        )

        val currentMonth = DateUtils.currentMonthIso()
        val monthsSet = entriesRaw.mapNotNull {
            if (it.productionDate.length >= 7) it.productionDate.substring(0, 7) else null
        }.toMutableSet().apply { add(currentMonth) }
        val sortedMonths = monthsSet.sortedDescending()
        val activeMonth = if (sortedMonths.contains(selectedMonth)) selectedMonth else currentMonth

        val monthChartMap = entriesRaw
            .filter { it.productionDate.startsWith(activeMonth) && it.productionDate.length >= 10 }
            .groupBy { it.productionDate.substring(8, 10).toIntOrNull() ?: 0 }
            .filterKeys { it > 0 }
            .toSortedMap()
        val monthlyChart = monthChartMap.map { (day, list) ->
            ChartPoint(label = day.toString(), value = list.sumOf { it.todayQuantity })
        }

        // Daily breakdown by line (if lines have today's output) or last 7 days trend
        val todayLinePoints = lineSummaries
            .filter { it.todayProduction > 0 || it.line.dailyTarget > 0 }
            .map { ChartPoint(label = it.line.lineName.take(10), value = it.todayProduction) }
        val dailyChart = if (todayLinePoints.isNotEmpty() && todayLinePoints.any { it.value > 0 }) {
            todayLinePoints
        } else {
            entriesRaw
                .groupBy { it.productionDate }
                .toSortedMap()
                .entries
                .toList()
                .takeLast(7)
                .map { (date, list) ->
                    val shortLabel = if (date.length >= 10) date.substring(5, 10) else date
                    ChartPoint(label = shortLabel, value = list.sumOf { it.todayQuantity })
                }
        }

        val editingDetail = editingId?.let { id -> entryDetails.firstOrNull { it.entry.id == id } }

        PmsUiState(
            language = language,
            buyers = buyerSummaries,
            styles = styleSummaries,
            purchaseOrders = poSummaries,
            lines = lineSummaries,
            productionEntries = entryDetails,
            dashboardStats = stats,
            availableMonths = sortedMonths,
            selectedDashboardMonth = activeMonth,
            monthlyOverviewChart = monthlyChart,
            dailyLineChart = dailyChart,
            editingEntry = editingDetail,
            preselectedPoId = preselectedPo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PmsUiState()
    )

    private fun currentStrings(): AppStrings =
        if (uiState.value.language == AppLanguage.BN) AppStringsBn else AppStringsEn

    private fun emitMessage(message: String) {
        _snackbarEvents.tryEmit(message)
    }

    fun toggleLanguage() {
        val next = if (uiState.value.language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
        viewModelScope.launch {
            prefs.setLanguage(next)
        }
    }

    fun selectDashboardMonth(month: String) {
        _selectedDashboardMonth.value = month
    }

    fun startEditingProductionEntry(entryId: String?) {
        _editingEntryId.value = entryId
        if (entryId != null) _preselectedPoId.value = null
    }

    fun selectPoForProduction(poId: String?) {
        _editingEntryId.value = null
        _preselectedPoId.value = poId
    }

    fun consumePreselectedPo() {
        _preselectedPoId.value = null
    }

    // ==================== BUYER CRUD ====================
    fun saveBuyer(
        editId: String?,
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String
    ): String? {
        val s = currentStrings()
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return s.errBuyerRequired
        val duplicate = uiState.value.buyers.any {
            it.buyer.name.equals(trimmed, ignoreCase = true) && it.buyer.id != editId
        }
        if (duplicate) return s.errBuyerDuplicate

        val existing = editId?.let { id -> uiState.value.buyers.firstOrNull { it.buyer.id == id }?.buyer }
        val entity = BuyerEntity(
            id = editId ?: DateUtils.newId(),
            name = trimmed,
            contactPerson = contactPerson.trim(),
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveBuyer(entity)
            emitMessage(if (editId == null) s.msgSaved else s.msgUpdated)
        }
        return null
    }

    fun deleteBuyer(buyerId: String) {
        val s = currentStrings()
        val hasStyles = uiState.value.styles.any { it.style.buyerId == buyerId }
        val hasPOs = uiState.value.purchaseOrders.any { it.po.buyerId == buyerId }
        if (hasStyles || hasPOs) {
            emitMessage(s.errBuyerHasStylesOrPos)
            return
        }
        viewModelScope.launch {
            repository.deleteBuyerById(buyerId)
            emitMessage(s.msgDeleted)
        }
    }

    // ==================== STYLE CRUD ====================
    fun saveStyle(
        editId: String?,
        buyerId: String,
        styleName: String,
        styleNumber: String,
        description: String
    ): String? {
        val s = currentStrings()
        val trimmed = styleName.trim()
        if (buyerId.isBlank() || trimmed.isEmpty()) return s.errStyleRequired
        val duplicate = uiState.value.styles.any {
            it.style.buyerId == buyerId &&
                it.style.styleName.equals(trimmed, ignoreCase = true) &&
                it.style.id != editId
        }
        if (duplicate) return s.errStyleDuplicate

        val existing = editId?.let { id -> uiState.value.styles.firstOrNull { it.style.id == id }?.style }
        val entity = StyleEntity(
            id = editId ?: DateUtils.newId(),
            buyerId = buyerId,
            styleName = trimmed,
            styleNumber = styleNumber.trim(),
            description = description.trim(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveStyle(entity)
            emitMessage(if (editId == null) s.msgSaved else s.msgUpdated)
        }
        return null
    }

    fun deleteStyle(styleId: String) {
        val s = currentStrings()
        val hasPOs = uiState.value.purchaseOrders.any { it.po.styleId == styleId }
        if (hasPOs) {
            emitMessage(s.errStyleHasPos)
            return
        }
        viewModelScope.launch {
            repository.deleteStyleById(styleId)
            emitMessage(s.msgDeleted)
        }
    }

    // ==================== PURCHASE ORDER CRUD ====================
    fun savePurchaseOrder(
        editId: String?,
        buyerId: String,
        styleId: String,
        poNumber: String,
        orderQtyText: String,
        orderDate: String,
        deliveryDate: String,
        unit: String,
        notes: String
    ): String? {
        val s = currentStrings()
        val trimmedPo = poNumber.trim()
        val qty = orderQtyText.trim().toIntOrNull()
        if (buyerId.isBlank() || styleId.isBlank() || trimmedPo.isEmpty()) return s.errPORequired
        if (qty == null || qty <= 0) return s.errPOOrderQtyPositive

        val duplicate = uiState.value.purchaseOrders.any {
            it.po.buyerId == buyerId &&
                it.po.poNumber.equals(trimmedPo, ignoreCase = true) &&
                it.po.id != editId
        }
        if (duplicate) return s.errPODuplicate

        if (editId != null) {
            val produced = calculateTotalProducedOnPo(editId, excludeEntryId = null)
            if (qty < produced) return s.errExceedsOrderQty
        }

        val existing = editId?.let { id -> uiState.value.purchaseOrders.firstOrNull { it.po.id == id }?.po }
        val entity = PurchaseOrderEntity(
            id = editId ?: DateUtils.newId(),
            buyerId = buyerId,
            styleId = styleId,
            poNumber = trimmedPo,
            orderQuantity = qty,
            orderDate = orderDate.trim(),
            deliveryDate = deliveryDate.trim(),
            unit = unit.trim().ifBlank { "Pcs" },
            notes = notes.trim(),
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.savePurchaseOrder(entity)
            emitMessage(if (editId == null) s.msgSaved else s.msgUpdated)
        }
        return null
    }

    fun deletePurchaseOrder(poId: String) {
        val s = currentStrings()
        val hasProduction = uiState.value.productionEntries.any { it.entry.poId == poId }
        if (hasProduction) {
            emitMessage(s.errPOHasProduction)
            return
        }
        viewModelScope.launch {
            repository.deletePurchaseOrderById(poId)
            emitMessage(s.msgDeleted)
        }
    }

    // ==================== PRODUCTION LINE CRUD ====================
    fun saveLine(
        editId: String?,
        lineName: String,
        supervisorName: String,
        dailyTargetText: String,
        workerCountText: String
    ): String? {
        val s = currentStrings()
        val trimmed = lineName.trim()
        if (trimmed.isEmpty()) return s.errLineRequired
        val target = dailyTargetText.trim().ifEmpty { "0" }.toIntOrNull()
        val workers = workerCountText.trim().ifEmpty { "0" }.toIntOrNull()
        if (target == null || target < 0 || workers == null || workers < 0) {
            return s.errNegativeQuantity
        }
        val duplicate = uiState.value.lines.any {
            it.line.lineName.equals(trimmed, ignoreCase = true) && it.line.id != editId
        }
        if (duplicate) return s.errLineDuplicate

        val existing = editId?.let { id -> uiState.value.lines.firstOrNull { it.line.id == id }?.line }
        val entity = ProductionLineEntity(
            id = editId ?: DateUtils.newId(),
            lineName = trimmed,
            supervisorName = supervisorName.trim(),
            dailyTarget = target,
            workerCount = workers,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveLine(entity)
            emitMessage(if (editId == null) s.msgSaved else s.msgUpdated)
        }
        return null
    }

    fun deleteLine(lineId: String) {
        val s = currentStrings()
        viewModelScope.launch {
            repository.deleteLineById(lineId)
            emitMessage(s.msgDeleted)
        }
    }

    // ==================== PRODUCTION ENTRY & CALCULATIONS ====================
    fun calculatePreviousProduction(poId: String, selectedDate: String, excludeEntryId: String?): Int {
        if (poId.isBlank() || selectedDate.isBlank()) return 0
        val editingCreatedAt = excludeEntryId?.let { id ->
            uiState.value.productionEntries.firstOrNull { it.entry.id == id }?.entry?.createdAt
        }
        return uiState.value.productionEntries
            .asSequence()
            .filter { it.entry.poId == poId && it.entry.id != excludeEntryId }
            .filter {
                it.entry.productionDate < selectedDate ||
                    (it.entry.productionDate == selectedDate &&
                        (editingCreatedAt == null || it.entry.createdAt < editingCreatedAt))
            }
            .sumOf { it.entry.todayQuantity }
    }

    fun calculateTotalProducedOnPo(poId: String, excludeEntryId: String?): Int {
        if (poId.isBlank()) return 0
        return uiState.value.productionEntries
            .asSequence()
            .filter { it.entry.poId == poId && it.entry.id != excludeEntryId }
            .sumOf { it.entry.todayQuantity }
    }

    fun saveProductionEntry(
        editId: String?,
        date: String,
        poId: String,
        lineId: String?,
        workerName: String,
        targetQtyText: String,
        mode: ProductionEntryMode,
        inputValueText: String
    ): String? {
        val s = currentStrings()
        if (date.isBlank() || poId.isBlank()) return s.errSelectPoAndDate

        val poSummary = uiState.value.purchaseOrders.firstOrNull { it.po.id == poId }
            ?: return s.errSelectPoAndDate

        val rawTarget = targetQtyText.trim().ifEmpty { "0" }.toIntOrNull()
        val rawInput = inputValueText.trim().toIntOrNull()
        if (rawTarget == null || rawTarget < 0 || rawInput == null || rawInput < 0) {
            return s.errNegativeQuantity
        }

        val previousBeforeDate = calculatePreviousProduction(poId, date, editId)
        val todayQty = when (mode) {
            ProductionEntryMode.CUMULATIVE_TOTAL -> {
                if (rawInput < previousBeforeDate) return s.errTotalBelowBefore
                rawInput - previousBeforeDate
            }
            ProductionEntryMode.DIRECT_TODAY -> rawInput
        }

        if (todayQty <= 0) return s.errZeroTodayQuantity

        val allOtherEntriesQty = calculateTotalProducedOnPo(poId, editId)
        val newTotalOnPo = allOtherEntriesQty + todayQty
        if (newTotalOnPo > poSummary.po.orderQuantity) {
            return s.errExceedsOrderQty
        }

        val existing = editId?.let { id ->
            uiState.value.productionEntries.firstOrNull { it.entry.id == id }?.entry
        }
        val entity = ProductionEntryEntity(
            id = editId ?: DateUtils.newId(),
            productionDate = date.trim(),
            poId = poId,
            lineId = lineId?.takeIf { it.isNotBlank() },
            workerName = workerName.trim(),
            targetQuantity = rawTarget,
            todayQuantity = todayQty,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.saveProductionEntry(entity)
            _editingEntryId.value = null
            _preselectedPoId.value = null
            emitMessage(if (editId == null) s.msgSaved else s.msgUpdated)
        }
        return null
    }

    fun deleteProductionEntry(entryId: String) {
        val s = currentStrings()
        viewModelScope.launch {
            repository.deleteProductionEntryById(entryId)
            if (_editingEntryId.value == entryId) {
                _editingEntryId.value = null
            }
            emitMessage(s.msgDeleted)
        }
    }

    // ==================== REPORTS AGGREGATION ====================
    fun buildReportRows(
        reportType: ReportType,
        startDate: String,
        endDate: String,
        searchQuery: String,
        sort: ReportSort
    ): Pair<List<ReportRow>, Int> {
        val state = uiState.value
        val filteredEntries = state.productionEntries.filter { detail ->
            val d = detail.entry.productionDate
            (startDate.isBlank() || d >= startDate) && (endDate.isBlank() || d <= endDate)
        }

        val poMap = state.purchaseOrders.associateBy { it.po.id }
        val linesMap = state.lines.associateBy { it.line.id }

        val rawRows: List<ReportRow> = when (reportType) {
            ReportType.DAILY -> {
                filteredEntries.groupBy { it.entry.productionDate }.map { (date, list) ->
                    ReportRow(
                        primaryLabel = date,
                        secondaryLabel = "${list.map { it.poNumber }.distinct().size} POs",
                        tertiaryLabel = "${list.mapNotNull { it.entry.lineId }.distinct().size} Lines",
                        entriesCount = list.size,
                        quantity = list.sumOf { it.entry.todayQuantity },
                        targetOrOrderQty = list.sumOf { it.entry.targetQuantity }
                    )
                }
            }
            ReportType.MONTHLY -> {
                filteredEntries
                    .groupBy { if (it.entry.productionDate.length >= 7) it.entry.productionDate.substring(0, 7) else it.entry.productionDate }
                    .map { (month, list) ->
                        ReportRow(
                            primaryLabel = DateUtils.formatMonthTitle(month),
                            secondaryLabel = month,
                            tertiaryLabel = "${list.map { it.entry.productionDate }.distinct().size} days",
                            entriesCount = list.size,
                            quantity = list.sumOf { it.entry.todayQuantity },
                            targetOrOrderQty = list.sumOf { it.entry.targetQuantity }
                        )
                    }
            }
            ReportType.PO_WISE -> {
                filteredEntries.groupBy { it.entry.poId }.mapNotNull { (poId, list) ->
                    val poSum = poMap[poId] ?: return@mapNotNull null
                    ReportRow(
                        primaryLabel = poSum.po.poNumber,
                        secondaryLabel = poSum.buyerName,
                        tertiaryLabel = poSum.styleName,
                        entriesCount = list.size,
                        quantity = list.sumOf { it.entry.todayQuantity },
                        targetOrOrderQty = poSum.po.orderQuantity,
                        remainingQty = poSum.remainingQty
                    )
                }
            }
            ReportType.BUYER_WISE -> {
                filteredEntries.groupBy { it.buyerId }.map { (buyerId, list) ->
                    val bName = list.firstOrNull()?.buyerName ?: "—"
                    val buyerPos = state.purchaseOrders.filter { it.po.buyerId == buyerId }
                    val buyerOrderTotal = buyerPos.sumOf { it.po.orderQuantity }
                    val buyerBalanceTotal = buyerPos.sumOf { it.remainingQty }
                    ReportRow(
                        primaryLabel = bName,
                        secondaryLabel = "${list.map { it.poNumber }.distinct().size} POs",
                        tertiaryLabel = "${list.map { it.styleName }.distinct().size} Styles",
                        entriesCount = list.size,
                        quantity = list.sumOf { it.entry.todayQuantity },
                        targetOrOrderQty = buyerOrderTotal,
                        remainingQty = buyerBalanceTotal
                    )
                }
            }
            ReportType.STYLE_WISE -> {
                filteredEntries.groupBy { it.styleId }.map { (styleId, list) ->
                    val first = list.firstOrNull()
                    val stylePos = state.purchaseOrders.filter { it.po.styleId == styleId }
                    val styleOrderTotal = stylePos.sumOf { it.po.orderQuantity }
                    val styleBalanceTotal = stylePos.sumOf { it.remainingQty }
                    ReportRow(
                        primaryLabel = first?.styleName ?: "—",
                        secondaryLabel = first?.buyerName ?: "—",
                        tertiaryLabel = "${list.map { it.poNumber }.distinct().size} POs",
                        entriesCount = list.size,
                        quantity = list.sumOf { it.entry.todayQuantity },
                        targetOrOrderQty = styleOrderTotal,
                        remainingQty = styleBalanceTotal
                    )
                }
            }
            ReportType.LINE_WISE -> {
                filteredEntries.groupBy { it.entry.lineId ?: "UNASSIGNED" }.map { (lineId, list) ->
                    val lineSum = linesMap[lineId]
                    val label = lineSum?.line?.lineName ?: currentStrings().optionalLine
                    val supervisor = lineSum?.line?.supervisorName ?: "—"
                    ReportRow(
                        primaryLabel = label,
                        secondaryLabel = supervisor,
                        tertiaryLabel = "${list.map { it.poNumber }.distinct().size} POs",
                        entriesCount = list.size,
                        quantity = list.sumOf { it.entry.todayQuantity },
                        targetOrOrderQty = lineSum?.line?.dailyTarget ?: 0
                    )
                }
            }
            ReportType.WORKER_WISE -> {
                filteredEntries
                    .groupBy { it.entry.workerName.trim().ifBlank { currentStrings().optionalLine } }
                    .map { (worker, list) ->
                        ReportRow(
                            primaryLabel = worker,
                            secondaryLabel = list.map { it.lineName }.filter { it.isNotBlank() }.distinct().joinToString(", ").ifBlank { "—" },
                            tertiaryLabel = "${list.map { it.poNumber }.distinct().size} POs",
                            entriesCount = list.size,
                            quantity = list.sumOf { it.entry.todayQuantity },
                            targetOrOrderQty = list.sumOf { it.entry.targetQuantity }
                        )
                    }
            }
        }

        val query = searchQuery.trim().lowercase()
        val searched = if (query.isEmpty()) rawRows else {
            rawRows.filter {
                it.primaryLabel.lowercase().contains(query) ||
                    it.secondaryLabel.lowercase().contains(query) ||
                    it.tertiaryLabel.lowercase().contains(query)
            }
        }

        val sorted = when (sort) {
            ReportSort.QTY_DESC -> searched.sortedByDescending { it.quantity }
            ReportSort.QTY_ASC -> searched.sortedBy { it.quantity }
            ReportSort.NAME_ASC -> searched.sortedBy { it.primaryLabel.lowercase() }
        }

        return sorted to filteredEntries.size
    }

    // ==================== BACKUP, RESTORE, SAMPLE & CLEAR ====================
    suspend fun buildBackupJson(): String = repository.exportBackupJson()

    fun restoreBackupJson(rawJson: String) {
        val s = currentStrings()
        viewModelScope.launch {
            val result = repository.restoreFromBackupJson(rawJson)
            if (result.isSuccess) {
                _editingEntryId.value = null
                _preselectedPoId.value = null
                emitMessage(s.msgBackupRestored)
            } else {
                emitMessage(s.msgInvalidBackup)
            }
        }
    }

    fun loadSampleFactoryData() {
        val s = currentStrings()
        viewModelScope.launch {
            repository.loadSampleFactoryData()
            _editingEntryId.value = null
            _preselectedPoId.value = null
            emitMessage(s.msgSampleLoaded)
        }
    }

    fun clearAllData() {
        val s = currentStrings()
        viewModelScope.launch {
            repository.clearAllData()
            _editingEntryId.value = null
            _preselectedPoId.value = null
            emitMessage(s.msgDataCleared)
        }
    }

    fun notifyMessage(message: String) {
        emitMessage(message)
    }
}
