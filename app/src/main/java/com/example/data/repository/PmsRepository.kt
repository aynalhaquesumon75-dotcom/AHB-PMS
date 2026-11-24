package com.example.data.repository

import com.example.data.local.BuyerEntity
import com.example.data.local.PmsDao
import com.example.data.local.ProductionEntryEntity
import com.example.data.local.ProductionLineEntity
import com.example.data.local.PurchaseOrderEntity
import com.example.data.local.StyleEntity
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PmsRepository(private val dao: PmsDao) {
    val buyers: Flow<List<BuyerEntity>> = dao.observeBuyers()
    val styles: Flow<List<StyleEntity>> = dao.observeStyles()
    val lines: Flow<List<ProductionLineEntity>> = dao.observeLines()
    val purchaseOrders: Flow<List<PurchaseOrderEntity>> = dao.observePurchaseOrders()
    val productionEntries: Flow<List<ProductionEntryEntity>> = dao.observeProductionEntries()

    suspend fun saveBuyer(buyer: BuyerEntity) = dao.upsertBuyer(buyer)
    suspend fun deleteBuyerById(id: String) = dao.deleteBuyerById(id)

    suspend fun saveStyle(style: StyleEntity) = dao.upsertStyle(style)
    suspend fun deleteStyleById(id: String) = dao.deleteStyleById(id)

    suspend fun saveLine(line: ProductionLineEntity) = dao.upsertLine(line)
    suspend fun deleteLineById(id: String) = dao.deleteLineById(id)

    suspend fun savePurchaseOrder(po: PurchaseOrderEntity) = dao.upsertPurchaseOrder(po)
    suspend fun deletePurchaseOrderById(id: String) = dao.deletePurchaseOrderById(id)

    suspend fun saveProductionEntry(entry: ProductionEntryEntity) = dao.upsertProductionEntry(entry)
    suspend fun deleteProductionEntryById(id: String) = dao.deleteProductionEntryById(id)

    suspend fun clearAllData() = dao.clearAllData()

    suspend fun exportBackupJson(): String {
        val buyersList = dao.getAllBuyersOnce()
        val stylesList = dao.getAllStylesOnce()
        val linesList = dao.getAllLinesOnce()
        val posList = dao.getAllPurchaseOrdersOnce()
        val entriesList = dao.getAllProductionEntriesOnce()

        val buyerMap = buyersList.associateBy { it.id }
        val styleMap = stylesList.associateBy { it.id }

        val root = JSONObject().apply {
            put("app", "AHB PMS")
            put("schemaVersion", 2)
            put("backupDate", DateUtils.todayIso())
            put("buyers", JSONArray().apply {
                buyersList.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("name", b.name)
                        put("contactPerson", b.contactPerson)
                        put("phone", b.phone)
                        put("email", b.email)
                        put("address", b.address)
                        put("createdAt", b.createdAt)
                    })
                }
            })
            put("styles", JSONArray().apply {
                stylesList.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("buyerId", s.buyerId)
                        put("buyer", buyerMap[s.buyerId]?.name ?: "")
                        put("name", s.styleName)
                        put("styleName", s.styleName)
                        put("styleNumber", s.styleNumber)
                        put("description", s.description)
                        put("createdAt", s.createdAt)
                    })
                }
            })
            put("lines", JSONArray().apply {
                linesList.forEach { l ->
                    put(JSONObject().apply {
                        put("id", l.id)
                        put("lineName", l.lineName)
                        put("supervisorName", l.supervisorName)
                        put("dailyTarget", l.dailyTarget)
                        put("workerCount", l.workerCount)
                        put("createdAt", l.createdAt)
                    })
                }
            })
            put("pos", JSONArray().apply {
                posList.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("buyerId", p.buyerId)
                        put("buyer", buyerMap[p.buyerId]?.name ?: "")
                        put("styleId", p.styleId)
                        put("style", styleMap[p.styleId]?.styleName ?: "")
                        put("po", p.poNumber)
                        put("poNumber", p.poNumber)
                        put("order", p.orderQuantity)
                        put("orderQuantity", p.orderQuantity)
                        put("orderDate", p.orderDate)
                        put("deliveryDate", p.deliveryDate)
                        put("unit", p.unit)
                        put("notes", p.notes)
                        put("createdAt", p.createdAt)
                    })
                }
            })
            put("productions", JSONArray().apply {
                entriesList.forEach { e ->
                    put(JSONObject().apply {
                        put("id", e.id)
                        put("date", e.productionDate)
                        put("productionDate", e.productionDate)
                        put("poId", e.poId)
                        put("lineId", e.lineId ?: JSONObject.NULL)
                        put("workerName", e.workerName)
                        put("targetQuantity", e.targetQuantity)
                        put("qty", e.todayQuantity)
                        put("todayQuantity", e.todayQuantity)
                        put("createdAt", e.createdAt)
                    })
                }
            })
        }
        return root.toString(2)
    }

    suspend fun restoreFromBackupJson(rawJson: String): Result<Unit> {
        return try {
            val root = JSONObject(rawJson)
            if (!root.has("buyers") || !root.has("pos") || !root.has("productions")) {
                return Result.failure(IllegalArgumentException("Missing required backup arrays"))
            }

            val buyersArr = root.getJSONArray("buyers")
            val stylesArr = root.optJSONArray("styles") ?: JSONArray()
            val linesArr = root.optJSONArray("lines") ?: JSONArray()
            val posArr = root.getJSONArray("pos")
            val prodArr = root.getJSONArray("productions")

            val parsedBuyers = mutableListOf<BuyerEntity>()
            val buyerIds = mutableSetOf<String>()
            for (i in 0 until buyersArr.length()) {
                val obj = buyersArr.getJSONObject(i)
                val id = obj.optString("id").ifBlank { DateUtils.newId() }
                val name = obj.optString("name").ifBlank { obj.optString("buyer") }.trim()
                if (name.isNotEmpty() && buyerIds.add(id)) {
                    parsedBuyers.add(
                        BuyerEntity(
                            id = id,
                            name = name,
                            contactPerson = obj.optString("contactPerson", ""),
                            phone = obj.optString("phone", ""),
                            email = obj.optString("email", ""),
                            address = obj.optString("address", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val parsedStyles = mutableListOf<StyleEntity>()
            val styleIds = mutableSetOf<String>()
            for (i in 0 until stylesArr.length()) {
                val obj = stylesArr.getJSONObject(i)
                val id = obj.optString("id").ifBlank { DateUtils.newId() }
                val buyerId = obj.optString("buyerId").ifBlank {
                    val bName = obj.optString("buyer")
                    parsedBuyers.firstOrNull { it.name.equals(bName, ignoreCase = true) }?.id ?: ""
                }
                val styleName = obj.optString("styleName").ifBlank {
                    obj.optString("name").ifBlank { obj.optString("style") }
                }.trim()
                if (buyerIds.contains(buyerId) && styleName.isNotEmpty() && styleIds.add(id)) {
                    parsedStyles.add(
                        StyleEntity(
                            id = id,
                            buyerId = buyerId,
                            styleName = styleName,
                            styleNumber = obj.optString("styleNumber", ""),
                            description = obj.optString("description", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val parsedLines = mutableListOf<ProductionLineEntity>()
            val lineIds = mutableSetOf<String>()
            for (i in 0 until linesArr.length()) {
                val obj = linesArr.getJSONObject(i)
                val id = obj.optString("id").ifBlank { DateUtils.newId() }
                val lineName = obj.optString("lineName").trim()
                if (lineName.isNotEmpty() && lineIds.add(id)) {
                    parsedLines.add(
                        ProductionLineEntity(
                            id = id,
                            lineName = lineName,
                            supervisorName = obj.optString("supervisorName", ""),
                            dailyTarget = obj.optInt("dailyTarget", 0).coerceAtLeast(0),
                            workerCount = obj.optInt("workerCount", 0).coerceAtLeast(0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val parsedPOs = mutableListOf<PurchaseOrderEntity>()
            val poIds = mutableSetOf<String>()
            for (i in 0 until posArr.length()) {
                val obj = posArr.getJSONObject(i)
                val id = obj.optString("id").ifBlank { DateUtils.newId() }
                val buyerId = obj.optString("buyerId").ifBlank {
                    val bName = obj.optString("buyer")
                    parsedBuyers.firstOrNull { it.name.equals(bName, ignoreCase = true) }?.id ?: ""
                }
                var styleId = obj.optString("styleId")
                if (styleId.isBlank() || !styleIds.contains(styleId)) {
                    val sName = obj.optString("style").trim()
                    if (sName.isNotEmpty() && buyerIds.contains(buyerId)) {
                        val existing = parsedStyles.firstOrNull {
                            it.buyerId == buyerId && it.styleName.equals(sName, ignoreCase = true)
                        }
                        styleId = if (existing != null) {
                            existing.id
                        } else {
                            val newStyleId = DateUtils.newId()
                            parsedStyles.add(StyleEntity(id = newStyleId, buyerId = buyerId, styleName = sName))
                            styleIds.add(newStyleId)
                            newStyleId
                        }
                    }
                }
                val poNum = obj.optString("poNumber").ifBlank {
                    obj.optString("po").ifBlank { obj.optString("number") }
                }.trim()
                val orderQty = obj.optInt("orderQuantity", obj.optInt("order", obj.optInt("qty", 0)))
                if (buyerIds.contains(buyerId) && styleIds.contains(styleId) && poNum.isNotEmpty() && orderQty > 0 && poIds.add(id)) {
                    parsedPOs.add(
                        PurchaseOrderEntity(
                            id = id,
                            buyerId = buyerId,
                            styleId = styleId,
                            poNumber = poNum,
                            orderQuantity = orderQty,
                            orderDate = obj.optString("orderDate", ""),
                            deliveryDate = obj.optString("deliveryDate", ""),
                            unit = obj.optString("unit", "Pcs").ifBlank { "Pcs" },
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val parsedEntries = mutableListOf<ProductionEntryEntity>()
            val entryIds = mutableSetOf<String>()
            for (i in 0 until prodArr.length()) {
                val obj = prodArr.getJSONObject(i)
                val id = obj.optString("id").ifBlank { DateUtils.newId() }
                val date = obj.optString("productionDate").ifBlank { obj.optString("date") }.trim()
                val poId = obj.optString("poId")
                val rawLineId = if (obj.isNull("lineId")) "" else obj.optString("lineId")
                val validLineId = if (rawLineId.isNotBlank() && lineIds.contains(rawLineId)) rawLineId else null
                val qty = obj.optInt("todayQuantity", obj.optInt("qty", 0))
                if (date.isNotEmpty() && poIds.contains(poId) && qty > 0 && entryIds.add(id)) {
                    parsedEntries.add(
                        ProductionEntryEntity(
                            id = id,
                            productionDate = date,
                            poId = poId,
                            lineId = validLineId,
                            workerName = obj.optString("workerName", ""),
                            targetQuantity = obj.optInt("targetQuantity", 0).coerceAtLeast(0),
                            todayQuantity = qty,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            dao.replaceAllData(
                buyers = parsedBuyers,
                styles = parsedStyles,
                lines = parsedLines,
                purchaseOrders = parsedPOs,
                productionEntries = parsedEntries
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadSampleFactoryData() {
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fun daysAgo(days: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -days)
            return iso.format(cal.time)
        }
        fun daysAhead(days: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, days)
            return iso.format(cal.time)
        }

        val b1 = BuyerEntity("b_hm", "H&M Global", "Karim Rahman", "+880 1711-223344", "sourcing@hm-partner.com", "Dhaka Liaison Office, Gulshan-2")
        val b2 = BuyerEntity("b_zara", "Inditex / Zara", "Nadia Islam", "+880 1819-556677", "orders@inditex-bd.com", "Banani Commercial Area, Dhaka")
        val b3 = BuyerEntity("b_uniqlo", "Uniqlo Fast Retailing", "Tanvir Ahmed", "+880 1912-889900", "qa@fastretailing-bd.com", "Uttara Sector 7, Dhaka")

        val s1 = StyleEntity("s_polo", b1.id, "Classic Pique Polo", "ST-1042", "220 GSM combed cotton pique polo shirt")
        val s2 = StyleEntity("s_hoodie", b1.id, "Fleece Zip Hoodie", "ST-2088", "320 GSM brushed fleece full-zip hoodie")
        val s3 = StyleEntity("s_chino", b2.id, "Slim Chino Trouser", "ZR-5501", "Stretch twill garment-washed chino")
        val s4 = StyleEntity("s_tee", b3.id, "Crewneck Dry Tee", "UQ-8810", "180 GSM moisture-wicking jersey tee")

        val l1 = ProductionLineEntity("l_1", "Line A1 (Sewing)", "Md. Rafiqul Islam", dailyTarget = 1200, workerCount = 42)
        val l2 = ProductionLineEntity("l_2", "Line B2 (Knitwear)", "Shahina Akter", dailyTarget = 1500, workerCount = 48)
        val l3 = ProductionLineEntity("l_3", "Line C1 (Denim/Twill)", "Jasim Uddin", dailyTarget = 950, workerCount = 36)

        val po1 = PurchaseOrderEntity("po_1", b1.id, s1.id, "PO-HM-2401", 6000, daysAgo(15), daysAhead(12), "Pcs", "Priority shipment - Navy & White")
        val po2 = PurchaseOrderEntity("po_2", b1.id, s2.id, "PO-HM-2409", 4000, daysAgo(10), daysAhead(20), "Pcs", "Autumn fleece batch")
        val po3 = PurchaseOrderEntity("po_3", b2.id, s3.id, "PO-ZR-8812", 5000, daysAgo(12), daysAhead(15), "Pcs", "QC inline inspection passed")
        val po4 = PurchaseOrderEntity("po_4", b3.id, s4.id, "PO-UQ-3304", 7500, daysAgo(8), daysAhead(18), "Pcs", "Export carton packing ready")

        val entries = listOf(
            ProductionEntryEntity("e_1", daysAgo(5), po1.id, l1.id, "Sadia Khatun", 1200, 1100),
            ProductionEntryEntity("e_2", daysAgo(4), po1.id, l1.id, "Sadia Khatun", 1200, 1180),
            ProductionEntryEntity("e_3", daysAgo(3), po3.id, l3.id, "Kamrul Hasan", 950, 920),
            ProductionEntryEntity("e_4", daysAgo(2), po2.id, l2.id, "Mehedi Hasan", 1500, 1340),
            ProductionEntryEntity("e_5", daysAgo(2), po4.id, l2.id, "Farhana Yasmin", 1500, 1420),
            ProductionEntryEntity("e_6", daysAgo(1), po1.id, l1.id, "Sadia Khatun", 1200, 1250),
            ProductionEntryEntity("e_7", daysAgo(1), po3.id, l3.id, "Kamrul Hasan", 950, 980),
            ProductionEntryEntity("e_8", daysAgo(0), po1.id, l1.id, "Sadia Khatun", 1200, 1050),
            ProductionEntryEntity("e_9", daysAgo(0), po4.id, l2.id, "Farhana Yasmin", 1500, 1480)
        )

        dao.replaceAllData(
            buyers = listOf(b1, b2, b3),
            styles = listOf(s1, s2, s3, s4),
            lines = listOf(l1, l2, l3),
            purchaseOrders = listOf(po1, po2, po3, po4),
            productionEntries = entries
        )
    }
}
