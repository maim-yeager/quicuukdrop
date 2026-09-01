package com.example.data.model

enum class DeviceType {
    PHONE,
    TABLET,
    LAPTOP,
    DESKTOP,
    UNKNOWN
}

enum class DeviceConnectionStatus {
    AVAILABLE,
    CONNECTING,
    CONNECTED,
    TRANSFERRING,
    DISCONNECTED
}

data class NearbyDevice(
    val endpointId: String,
    val deviceName: String,
    val deviceType: DeviceType = DeviceType.PHONE,
    val signalStrength: Int = 4, // 1 to 4 bars
    val distanceMeters: Float = 2.5f,
    val status: DeviceConnectionStatus = DeviceConnectionStatus.AVAILABLE
)
