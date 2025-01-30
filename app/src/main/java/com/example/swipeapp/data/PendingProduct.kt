package com.example.swipeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_products")
data class PendingProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val productType: String,
    val price: Double,
    val tax: Double,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
} 