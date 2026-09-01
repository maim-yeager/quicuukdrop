package com.example.data.nearby

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.example.data.model.DeviceConnectionStatus
import com.example.data.model.DeviceType
import com.example.data.model.NearbyDevice
import com.example.data.model.SharedFile
import com.example.data.model.TransferState
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class NearbyTransferService(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val tag = "NearbyTransferService"
    private val serviceId = "com.aistudio.quickdrop"
    private val strategy = Strategy.P2P_STAR

    private val connectionsClient: ConnectionsClient by lazy {
        Nearby.getConnectionsClient(context)
    }

    private val _discoveredDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<NearbyDevice>> = _discoveredDevices.asStateFlow()

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private var currentConnectedEndpointId: String? = null
    private var currentConnectedDeviceName: String = "Nearby Device"
    private var isAdvertising = false
    private var isDiscovering = false
    private var transferStartTime = 0L
    private var speedCalculationJob: Job? = null

    // Track transfer progress
    private var totalBytesToTransfer = 0L
    private var bytesCurrentlyTransferred = 0L
    private var totalFilesCount = 0
    private var currentFileIndex = 0
    private var currentFileName = ""

    // Outgoing tracking
    private var sendFileNames: List<String> = emptyList()

    // Incoming tracking
    private var incomingFileName = ""
    private var incomingFileMimeType = "application/octet-stream"
    private var incomingFilesReceived = 0

    // Ids of payloads that carry actual file bytes (vs metadata)
    private val filePayloadIds = mutableSetOf<Long>()

    // Callbacks for UI
    var onTransferCompletedListener: ((deviceName: String, isOutgoing: Boolean, fileCount: Int, totalBytes: Long) -> Unit)? = null

    // Files queued locally, sent for real once the outgoing connection is accepted
    private var pendingFilesToSend: List<SharedFile> = emptyList()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(tag, "Connection initiated with: ${info.endpointName}")
            currentConnectedEndpointId = endpointId
            currentConnectedDeviceName = info.endpointName

            if (info.isIncomingConnection) {
                // An incoming request from another device: ask the user to accept.
                _transferState.value = TransferState.ConnectionRequested(
                    endpointId = endpointId,
                    deviceName = info.endpointName,
                    fileCount = if (totalFilesCount > 0) totalFilesCount else 1,
                    totalBytes = if (totalBytesToTransfer > 0) totalBytesToTransfer else 0L,
                    isIncoming = true
                )
            } else {
                // We initiated this connection; keep waiting for the handshake to complete.
                if (_transferState.value !is TransferState.InProgress) {
                    _transferState.value = TransferState.Connecting(
                        NearbyDevice(
                            endpointId = endpointId,
                            deviceName = info.endpointName
                        )
                    )
                }
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(tag, "Connected to $endpointId")
                    currentConnectedEndpointId = endpointId

                    // If we initiated this connection to send files, kick off the real transfer now
                    if (pendingFilesToSend.isNotEmpty()) {
                        val files = pendingFilesToSend
                        pendingFilesToSend = emptyList()
                        transferStartTime = System.currentTimeMillis()
                        totalFilesCount = files.size
                        totalBytesToTransfer = files.sumOf { it.size }
                        sendFileNames = files.map { it.name }
                        currentFileIndex = 1
                        currentFileName = files.first().name
                        bytesCurrentlyTransferred = 0L
                        sendFiles(endpointId, files)
                    } else {
                        // Incoming connection accepted by this device: reset to idle (payload callback drives state)
                        _transferState.value = TransferState.Idle
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.w(tag, "Connection rejected by $endpointId")
                    _transferState.value = TransferState.Failed("Connection was declined by the device.")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e(tag, "Connection failed to $endpointId")
                    _transferState.value = TransferState.Failed("Connection error occurred.")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(tag, "Disconnected from $endpointId")
            currentConnectedEndpointId = null
            speedCalculationJob?.cancel()
            if (_transferState.value is TransferState.InProgress) {
                _transferState.value = TransferState.Failed("The device disconnected during the transfer.")
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(tag, "Endpoint found: ${info.endpointName} ($endpointId)")
            val deviceType = when {
                info.endpointName.contains("iPhone", true) -> DeviceType.PHONE
                info.endpointName.contains("iPad", true) || info.endpointName.contains("Tab", true) -> DeviceType.TABLET
                info.endpointName.contains("Mac", true) || info.endpointName.contains("Laptop", true) || info.endpointName.contains("PC", true) -> DeviceType.LAPTOP
                else -> DeviceType.PHONE
            }

            val device = NearbyDevice(
                endpointId = endpointId,
                deviceName = info.endpointName,
                deviceType = deviceType,
                signalStrength = (3..4).random(),
                distanceMeters = (1.5f + (Math.random() * 4.0).toFloat()),
                status = DeviceConnectionStatus.AVAILABLE
            )

            val currentList = _discoveredDevices.value.filter { it.endpointId != endpointId }
            _discoveredDevices.value = currentList + device
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(tag, "Endpoint lost: $endpointId")
            _discoveredDevices.value = _discoveredDevices.value.filter { it.endpointId != endpointId }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val metadataJson = String(payload.asBytes() ?: ByteArray(0), StandardCharsets.UTF_8)
                try {
                    val json = JSONObject(metadataJson)
                    when (json.optString("type", "batch")) {
                        "file" -> {
                            // Per-file metadata that immediately precedes a file payload
                            incomingFileName = json.optString("name", "Incoming file")
                            incomingFileMimeType = json.optString("mime", "application/octet-stream")
                        }
                        else -> {
                            // Batch-level metadata for the whole transfer
                            totalFilesCount = json.optInt("fileCount", 1)
                            totalBytesToTransfer = json.optLong("totalBytes", 0L)
                            currentFileName = json.optString("firstFileName", "Incoming file")
                            transferStartTime = System.currentTimeMillis()
                            currentFileIndex = 1
                            _transferState.value = TransferState.InProgress(
                                deviceName = currentConnectedDeviceName,
                                isOutgoing = false,
                                totalFiles = totalFilesCount,
                                currentFileIndex = 1,
                                currentFileName = currentFileName,
                                bytesTransferred = 0L,
                                totalBytes = totalBytesToTransfer,
                                speedMBps = 0.0,
                                secondsRemaining = 0L
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing payload metadata", e)
                }
            } else if (payload.type == Payload.Type.FILE) {
                val filePayload = payload.asFile()
                val fileName = incomingFileName.ifBlank {
                    "Incoming_${System.currentTimeMillis()}"
                }
                val mime = incomingFileMimeType
                val fileSize = filePayload?.size ?: 0L

                // Track this payload so its progress updates count towards the file transfer
                filePayloadIds.add(payload.id)

                // Save the incoming file off the main thread
                val pfd = filePayload?.asParcelFileDescriptor()
                if (pfd != null) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            saveIncomingFile(pfd, fileName, mime)
                        }
                        bytesCurrentlyTransferred += fileSize
                        incomingFilesReceived += 1
                        currentFileIndex = incomingFilesReceived
                        currentFileName = fileName
                        val elapsedSeconds = ((System.currentTimeMillis() - transferStartTime) / 1000.0).coerceAtLeast(0.1)
                        val speedMBps = (bytesCurrentlyTransferred / (1024.0 * 1024.0)) / elapsedSeconds
                        val bytesRemaining = (totalBytesToTransfer - bytesCurrentlyTransferred).coerceAtLeast(0L)
                        val secondsRemaining = if (speedMBps > 0) (bytesRemaining / (speedMBps * 1024 * 1024)).toLong() else 0L

                        _transferState.value = TransferState.InProgress(
                            deviceName = currentConnectedDeviceName,
                            isOutgoing = false,
                            totalFiles = totalFilesCount.coerceAtLeast(currentFileIndex),
                            currentFileIndex = currentFileIndex,
                            currentFileName = currentFileName,
                            bytesTransferred = bytesCurrentlyTransferred,
                            totalBytes = totalBytesToTransfer,
                            speedMBps = speedMBps,
                            secondsRemaining = secondsRemaining
                        )

                        if (incomingFilesReceived >= totalFilesCount && totalFilesCount > 0) {
                            val finalBytes = maxOf(totalBytesToTransfer, bytesCurrentlyTransferred)
                            _transferState.value = TransferState.Completed(
                                deviceName = currentConnectedDeviceName,
                                isOutgoing = false,
                                fileCount = incomingFilesReceived,
                                totalBytes = finalBytes,
                                durationSeconds = ((System.currentTimeMillis() - transferStartTime) / 1000L).coerceAtLeast(1L)
                            )
                            onTransferCompletedListener?.invoke(currentConnectedDeviceName, false, incomingFilesReceived, finalBytes)
                            filePayloadIds.clear()
                        }
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when (update.status) {
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    bytesCurrentlyTransferred = update.bytesTransferred
                    val total = update.totalBytes
                    val elapsedSeconds = ((System.currentTimeMillis() - transferStartTime) / 1000.0).coerceAtLeast(0.1)
                    val speedMBps = (bytesCurrentlyTransferred / (1024.0 * 1024.0)) / elapsedSeconds
                    val bytesRemaining = (total - bytesCurrentlyTransferred).coerceAtLeast(0L)
                    val secondsRemaining = if (speedMBps > 0) (bytesRemaining / (speedMBps * 1024 * 1024)).toLong() else 0L

                    _transferState.value = TransferState.InProgress(
                        deviceName = currentConnectedDeviceName,
                        isOutgoing = sendFileNames.isNotEmpty(),
                        totalFiles = totalFilesCount.coerceAtLeast(1),
                        currentFileIndex = currentFileIndex.coerceAtLeast(1),
                        currentFileName = currentFileName.ifBlank { "Transferring..." },
                        bytesTransferred = bytesCurrentlyTransferred,
                        totalBytes = total,
                        speedMBps = speedMBps.coerceAtLeast(0.0),
                        secondsRemaining = secondsRemaining
                    )
                }
                PayloadTransferUpdate.Status.SUCCESS -> {
                    if (update.payloadId in filePayloadIds) {
                        // One file finished transferring
                        if (sendFileNames.isNotEmpty()) {
                            // Outgoing: advance to the next queued file
                            if (currentFileIndex < totalFilesCount) {
                                currentFileIndex += 1
                                currentFileName = sendFileNames.getOrNull(currentFileIndex - 1) ?: currentFileName
                            }
                            if (currentFileIndex >= totalFilesCount && bytesCurrentlyTransferred >= totalBytesToTransfer) {
                                finishOutgoingTransfer()
                            }
                        }
                    }
                }
                PayloadTransferUpdate.Status.CANCELED -> {
                    _transferState.value = TransferState.Cancelled
                }
                PayloadTransferUpdate.Status.FAILURE -> {
                    _transferState.value = TransferState.Failed("Transfer interrupted or failed.")
                }
            }
        }
    }

    private fun finishOutgoingTransfer() {
        val duration = ((System.currentTimeMillis() - transferStartTime) / 1000L).coerceAtLeast(1L)
        val finalBytes = if (totalBytesToTransfer > 0) totalBytesToTransfer else bytesCurrentlyTransferred
        val finalFiles = if (totalFilesCount > 0) totalFilesCount else 1

        _transferState.value = TransferState.Completed(
            deviceName = currentConnectedDeviceName,
            isOutgoing = true,
            fileCount = finalFiles,
            totalBytes = finalBytes,
            durationSeconds = duration
        )
        onTransferCompletedListener?.invoke(currentConnectedDeviceName, true, finalFiles, finalBytes)
        filePayloadIds.clear()
        sendFileNames = emptyList()
    }

    private fun saveIncomingFile(pfd: ParcelFileDescriptor, fileName: String, mimeType: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QuickDrop")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        val input = ParcelFileDescriptor.AutoCloseInputStream(pfd)
                        input.use { inputStream -> inputStream.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val qdFolder = File(downloadsDir, "QuickDrop")
                if (!qdFolder.exists()) {
                    qdFolder.mkdirs()
                }
                val target = File(qdFolder, fileName)
                FileOutputStream(target).use { out ->
                    val input = ParcelFileDescriptor.AutoCloseInputStream(pfd)
                    input.use { inputStream -> inputStream.copyTo(out) }
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(target.absolutePath),
                    arrayOf(mimeType),
                    null
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to save incoming file: $fileName", e)
        } finally {
            try {
                pfd.close()
            } catch (_: Exception) {}
        }
    }

    fun startAdvertising(deviceName: String) {
        stopAll()
        isAdvertising = true
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()

        try {
            connectionsClient.startAdvertising(
                deviceName,
                serviceId,
                connectionLifecycleCallback,
                options
            ).addOnSuccessListener {
                Log.d(tag, "Started advertising as $deviceName")
            }.addOnFailureListener { e ->
                Log.e(tag, "Failed to start advertising", e)
            }
        } catch (e: Exception) {
            Log.e(tag, "Nearby API advertising error", e)
        }
    }

    fun startDiscovery() {
        stopAll()
        isDiscovering = true
        _discoveredDevices.value = emptyList()
        _transferState.value = TransferState.Searching()

        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()

        try {
            connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                options
            ).addOnSuccessListener {
                Log.d(tag, "Started discovery")
            }.addOnFailureListener { e ->
                Log.e(tag, "Failed to start discovery", e)
            }
        } catch (e: Exception) {
            Log.e(tag, "Nearby API discovery error", e)
        }
    }

    fun connectToDevice(device: NearbyDevice, localName: String, filesToSend: List<SharedFile> = emptyList()) {
        pendingFilesToSend = filesToSend
        if (filesToSend.isNotEmpty()) {
            totalFilesCount = filesToSend.size
            totalBytesToTransfer = filesToSend.sumOf { it.size }
        }
        _transferState.value = TransferState.Connecting(device)
        try {
            connectionsClient.requestConnection(
                localName,
                device.endpointId,
                connectionLifecycleCallback
            ).addOnSuccessListener {
                Log.d(tag, "Connection requested to ${device.deviceName}")
            }.addOnFailureListener { e ->
                Log.e(tag, "Request connection failed", e)
                _transferState.value = TransferState.Failed("Could not connect to ${device.deviceName}")
            }
        } catch (e: Exception) {
            Log.e(tag, "Nearby API connect error", e)
        }
    }

    fun acceptConnection(endpointId: String) {
        try {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Log.d(tag, "Accepted connection from $endpointId")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Failed to accept connection", e)
                    _transferState.value = TransferState.Failed("Could not accept the connection.")
                }
        } catch (e: Exception) {
            Log.e(tag, "Accept connection error", e)
        }
    }

    fun rejectConnection(endpointId: String) {
        try {
            connectionsClient.rejectConnection(endpointId)
            _transferState.value = TransferState.Idle
        } catch (e: Exception) {
            Log.e(tag, "Reject connection error", e)
        }
    }

    fun sendFiles(endpointId: String, files: List<SharedFile>) {
        if (files.isEmpty()) return

        transferStartTime = System.currentTimeMillis()
        totalFilesCount = files.size
        totalBytesToTransfer = files.sumOf { it.size }
        sendFileNames = files.map { it.name }
        filePayloadIds.clear()
        currentFileIndex = 1
        currentFileName = files.first().name

        // Send metadata payload first
        try {
            val json = JSONObject().apply {
                put("type", "batch")
                put("fileCount", files.size)
                put("totalBytes", totalBytesToTransfer)
                put("firstFileName", currentFileName)
            }
            val metadataPayload = Payload.fromBytes(json.toString().toByteArray(StandardCharsets.UTF_8))
            connectionsClient.sendPayload(endpointId, metadataPayload)

            // Send actual file payloads, each preceded by its own metadata
            for (file in files) {
                try {
                    val fileMeta = JSONObject().apply {
                        put("type", "file")
                        put("name", file.name)
                        put("size", file.size)
                        put("mime", file.mimeType)
                    }
                    val metaPayload = Payload.fromBytes(fileMeta.toString().toByteArray(StandardCharsets.UTF_8))
                    connectionsClient.sendPayload(endpointId, metaPayload)

                    val pfd: ParcelFileDescriptor? = context.contentResolver.openFileDescriptor(file.uri, "r")
                    if (pfd != null) {
                        val filePayload = Payload.fromFile(pfd)
                        filePayloadIds.add(filePayload.id)
                        connectionsClient.sendPayload(endpointId, filePayload)
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error opening file descriptor for ${file.name}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error sending payloads", e)
        }
    }

    fun cancelTransfer() {
        speedCalculationJob?.cancel()
        clearTransferState()
        _transferState.value = TransferState.Cancelled
        stopAll()
    }

    fun resetState() {
        clearTransferState()
        _transferState.value = TransferState.Idle
        _discoveredDevices.value = emptyList()
    }

    private fun clearTransferState() {
        totalBytesToTransfer = 0L
        bytesCurrentlyTransferred = 0L
        totalFilesCount = 0
        currentFileIndex = 0
        currentFileName = ""
        incomingFileName = ""
        incomingFileMimeType = "application/octet-stream"
        incomingFilesReceived = 0
        sendFileNames = emptyList()
        filePayloadIds.clear()
        pendingFilesToSend = emptyList()
    }

    fun stopDiscovery() {
        if (isDiscovering) {
            try {
                connectionsClient.stopDiscovery()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping discovery", e)
            }
            isDiscovering = false
        }
    }

    fun stopAdvertising() {
        if (isAdvertising) {
            try {
                connectionsClient.stopAdvertising()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping advertising", e)
            }
            isAdvertising = false
        }
    }

    fun stopAll() {
        try {
            stopAdvertising()
            stopDiscovery()
            connectionsClient.stopAllEndpoints()
        } catch (e: Exception) {
            Log.e(tag, "Error in stopAll", e)
        }
    }
}
