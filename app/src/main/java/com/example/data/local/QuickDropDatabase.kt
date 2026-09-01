package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TransferEntity::class], version = 1, exportSchema = false)
abstract class QuickDropDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao

    companion object {
        @Volatile
        private var INSTANCE: QuickDropDatabase? = null

        fun getDatabase(context: Context): QuickDropDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuickDropDatabase::class.java,
                    "quickdrop_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
