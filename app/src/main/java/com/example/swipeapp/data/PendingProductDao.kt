package com.example.swipeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingProductDao {
    @Query("SELECT * FROM pending_products WHERE syncStatus != 'SYNCED' ORDER BY timestamp ASC")
    fun getPendingProducts(): Flow<List<PendingProduct>>

    @Insert
    suspend fun insert(product: PendingProduct): Long

    @Update
    suspend fun update(product: PendingProduct)

    @Query("UPDATE pending_products SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query("DELETE FROM pending_products WHERE syncStatus = 'SYNCED'")
    suspend fun deleteSyncedProducts()
} 