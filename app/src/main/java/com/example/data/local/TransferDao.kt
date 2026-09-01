package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_history ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfer_history WHERE isOutgoing = 1 ORDER BY timestamp DESC")
    fun getSentTransfers(): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfer_history WHERE isOutgoing = 0 ORDER BY timestamp DESC")
    fun getReceivedTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity): Long

    @Query("DELETE FROM transfer_history WHERE id = :id")
    suspend fun deleteTransferById(id: Long)

    @Query("DELETE FROM transfer_history")
    suspend fun clearAll()
}
