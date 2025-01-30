package com.example.swipeapp.work

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swipeapp.data.AppDatabase
import com.example.swipeapp.data.SyncStatus
import com.example.swipeapp.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val productService = NetworkModule.productService

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingProducts = database.pendingProductDao()
                .getPendingProducts().first()

            for (product in pendingProducts) {
                try {
                    database.pendingProductDao().updateSyncStatus(product.id, SyncStatus.SYNCING)

                    val productName = product.productName
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val productType = product.productType
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val price = product.price.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val tax = product.tax.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())

                    val imagePart = product.imagePath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("files[]", file.name, requestFile)
                        } else null
                    }

                    val response = productService.addProduct(
                        productName,
                        productType,
                        price,
                        tax,
                        imagePart
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        database.pendingProductDao().updateSyncStatus(product.id, SyncStatus.SYNCED)
                    } else {
                        database.pendingProductDao().updateSyncStatus(product.id, SyncStatus.FAILED)
                    }
                } catch (e: Exception) {
                    database.pendingProductDao().updateSyncStatus(product.id, SyncStatus.FAILED)
                }
            }

            // Clean up synced products
            database.pendingProductDao().deleteSyncedProducts()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 