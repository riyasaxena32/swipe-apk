package com.example.swipeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PendingProduct::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingProductDao(): PendingProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swipe_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 