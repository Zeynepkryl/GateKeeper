package com.zeynep.gatekeeper.presentation.scanner

import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState

data class ScannerUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val receivedPackets: List<BiometricReading> = emptyList(),
    val consecutiveCount: Int = 0,
    val errorMessage: String? = null,
    val isAutoRetrying: Boolean = false,
    val retryAttempt: Int = 0
) {
    val isIdle: Boolean get() = connectionState == ConnectionState.Disconnected
    val isConnecting: Boolean get() = connectionState == ConnectionState.Connecting
    val isReady: Boolean get() = connectionState == ConnectionState.Ready
    val hasError: Boolean get() = connectionState == ConnectionState.Error
    val canStartScan: Boolean get() = (isIdle || hasError) && !isAutoRetrying
}
