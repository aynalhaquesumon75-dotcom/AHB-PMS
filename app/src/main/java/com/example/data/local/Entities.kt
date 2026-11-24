package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "buyers")
data class BuyerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "styles",
    foreignKeys = [
        ForeignKey(
            entity = BuyerEntity::class,
            parentColumns = ["id"],
            childColumns = ["buyerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("buyerId")]
)
data class StyleEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val styleName: String,
    val styleNumber: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "production_lines")
data class ProductionLineEntity(
    @PrimaryKey val id: String,
    val lineName: String,
    val supervisorName: String = "",
    val dailyTarget: Int = 0,
    val workerCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "purchase_orders",
    foreignKeys = [
        ForeignKey(
            entity = BuyerEntity::class,
            parentColumns = ["id"],
            childColumns = ["buyerId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = StyleEntity::class,
            parentColumns = ["id"],
            childColumns = ["styleId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("buyerId"), Index("styleId")]
)
data class PurchaseOrderEntity(
    @PrimaryKey val id: String,
    val buyerId: String,
    val styleId: String,
    val poNumber: String,
    val orderQuantity: Int,
    val orderDate: String = "",
    val deliveryDate: String = "",
    val unit: String = "Pcs",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "production_entries",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["poId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ProductionLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("poId"), Index("lineId"), Index("productionDate")]
)
data class ProductionEntryEntity(
    @PrimaryKey val id: String,
    val productionDate: String,
    val poId: String,
    val lineId: String? = null,
    val workerName: String = "",
    val targetQuantity: Int = 0,
    val todayQuantity: Int,
    val createdAt: Long = System.currentTimeMillis()
)
