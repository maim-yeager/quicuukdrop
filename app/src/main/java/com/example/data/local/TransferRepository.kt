package com.example.data.local

import kotlinx.coroutines.flow.Flow

class TransferRepository(private val transferDao: TransferDao) {
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()
    val sentTransfers: Flow<List<TransferEntity>> = transferDao.getSentTransfers()
    val receivedTransfers: Flow<List<TransferEntity>> = transferDao.getReceivedTransfers()

    suspend fun insertTransfer(transfer: TransferEntity): Long {
        return transferDao.insertTransfer(transfer)
    }

    suspend fun deleteTransfer(id: Long) {
        transferDao.deleteTransferById(id)
    }

    suspend fun clearHistory() {
        transferDao.clearAll()
    }
}
