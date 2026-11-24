package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PmsDao {
    // Buyers
    @Query("SELECT * FROM buyers ORDER BY name COLLATE NOCASE ASC")
    fun observeBuyers(): Flow<List<BuyerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBuyer(buyer: BuyerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyers(buyers: List<BuyerEntity>)

    @Delete
    suspend fun deleteBuyer(buyer: BuyerEntity)

    @Query("DELETE FROM buyers WHERE id = :id")
    suspend fun deleteBuyerById(id: String)

    // Styles
    @Query("SELECT * FROM styles ORDER BY styleName COLLATE NOCASE ASC")
    fun observeStyles(): Flow<List<StyleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStyle(style: StyleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStyles(styles: List<StyleEntity>)

    @Query("DELETE FROM styles WHERE id = :id")
    suspend fun deleteStyleById(id: String)

    // Production Lines
    @Query("SELECT * FROM production_lines ORDER BY lineName COLLATE NOCASE ASC")
    fun observeLines(): Flow<List<ProductionLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLine(line: ProductionLineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<ProductionLineEntity>)

    @Query("DELETE FROM production_lines WHERE id = :id")
    suspend fun deleteLineById(id: String)

    // Purchase Orders
    @Query("SELECT * FROM purchase_orders ORDER BY createdAt DESC")
    fun observePurchaseOrders(): Flow<List<PurchaseOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPurchaseOrder(po: PurchaseOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseOrders(pos: List<PurchaseOrderEntity>)

    @Query("DELETE FROM purchase_orders WHERE id = :id")
    suspend fun deletePurchaseOrderById(id: String)

    // Production Entries
    @Query("SELECT * FROM production_entries ORDER BY productionDate DESC, createdAt DESC")
    fun observeProductionEntries(): Flow<List<ProductionEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProductionEntry(entry: ProductionEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionEntries(entries: List<ProductionEntryEntity>)

    @Query("DELETE FROM production_entries WHERE id = :id")
    suspend fun deleteProductionEntryById(id: String)

    // Snapshot / Backup helpers
    @Query("SELECT * FROM buyers")
    suspend fun getAllBuyersOnce(): List<BuyerEntity>

    @Query("SELECT * FROM styles")
    suspend fun getAllStylesOnce(): List<StyleEntity>

    @Query("SELECT * FROM production_lines")
    suspend fun getAllLinesOnce(): List<ProductionLineEntity>

    @Query("SELECT * FROM purchase_orders")
    suspend fun getAllPurchaseOrdersOnce(): List<PurchaseOrderEntity>

    @Query("SELECT * FROM production_entries")
    suspend fun getAllProductionEntriesOnce(): List<ProductionEntryEntity>

    @Query("DELETE FROM production_entries")
    suspend fun clearProductionEntries()

    @Query("DELETE FROM purchase_orders")
    suspend fun clearPurchaseOrders()

    @Query("DELETE FROM styles")
    suspend fun clearStyles()

    @Query("DELETE FROM production_lines")
    suspend fun clearLines()

    @Query("DELETE FROM buyers")
    suspend fun clearBuyers()

    @Transaction
    suspend fun clearAllData() {
        clearProductionEntries()
        clearPurchaseOrders()
        clearStyles()
        clearLines()
        clearBuyers()
    }

    @Transaction
    suspend fun replaceAllData(
        buyers: List<BuyerEntity>,
        styles: List<StyleEntity>,
        lines: List<ProductionLineEntity>,
        purchaseOrders: List<PurchaseOrderEntity>,
        productionEntries: List<ProductionEntryEntity>
    ) {
        clearAllData()
        insertBuyers(buyers)
        insertStyles(styles)
        insertLines(lines)
        insertPurchaseOrders(purchaseOrders)
        insertProductionEntries(productionEntries)
    }
}
