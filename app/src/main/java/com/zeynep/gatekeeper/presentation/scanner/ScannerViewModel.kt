package com.zeynep.gatekeeper.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.domain.usecase.ConnectScannerUseCase
import com.zeynep.gatekeeper.domain.usecase.DisconnectScannerUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveBiometricStreamUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveConnectionStateUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveErrorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

/**
 * Orchestrates the biometric scanning flow, managing hardware connection
 * lifecycle, real-time data collection, and navigation events.
 *
 * Key responsibilities:
 * - Bridges domain use cases to the UI layer via [ScannerUiState].
 * - Tracks [REQUIRED_CONSECUTIVE_PACKETS] successful readings to trigger navigation.
 * - Implements exponential backoff auto-retry on hardware failures.
 * - Ensures hardware cleanup in [onCleared] to prevent listener leaks.
 *
 * Navigation events are emitted through a [Channel] to guarantee
 * exactly-once delivery, avoiding re-navigation on recomposition.
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val connectScanner: ConnectScannerUseCase,
    private val disconnectScanner: DisconnectScannerUseCase,
    private val observeBiometricStream: ObserveBiometricStreamUseCase,
    private val observeConnectionState: ObserveConnectionStateUseCase,
    private val observeErrors: ObserveErrorsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _events = Channel<ScannerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var autoRetryJob: Job? = null

    init {
        collectConnectionState()
        collectBiometricData()
        collectErrors()
    }

    fun startScan() = performScan(autoRetry = false)

    fun retry() = performScan(autoRetry = false)

    fun startScanWithAutoRetry() = performScan(autoRetry = true)

    private fun performScan(autoRetry: Boolean) {
        autoRetryJob?.cancel()
        disconnectScanner()
        _uiState.update {
            it.copy(
                errorMessage = null,
                consecutiveCount = 0,
                receivedPackets = emptyList(),
                isAutoRetrying = autoRetry,
                retryAttempt = 0
            )
        }
        viewModelScope.launch { connectScanner() }
    }

    private fun collectConnectionState() {
        viewModelScope.launch {
            observeConnectionState().collect { state ->
                _uiState.update { current ->
                    current.copy(
                        connectionState = state,
                        consecutiveCount = if (state == ConnectionState.Error) 0
                        else current.consecutiveCount
                    )
                }
                if (state == ConnectionState.Error && _uiState.value.isAutoRetrying) {
                    scheduleRetry()
                }
            }
        }
    }

    private fun collectBiometricData() {
        viewModelScope.launch {
            observeBiometricStream().collect { reading ->
                _uiState.update { current ->
                    current.copy(
                        receivedPackets = current.receivedPackets + reading,
                        consecutiveCount = current.consecutiveCount + 1
                    )
                }
                val snapshot = _uiState.value
                if (snapshot.consecutiveCount >= REQUIRED_CONSECUTIVE_PACKETS) {
                    val successPackets =
                        snapshot.receivedPackets.takeLast(REQUIRED_CONSECUTIVE_PACKETS)
                    handleScanComplete(successPackets)
                }
            }
        }
    }

    private fun collectErrors() {
        viewModelScope.launch {
            observeErrors().collect { message ->
                _uiState.update { it.copy(errorMessage = message) }
            }
        }
    }

    private fun handleScanComplete(packets: List<BiometricReading>) {
        autoRetryJob?.cancel()
        disconnectScanner()
        _uiState.update { ScannerUiState() }
        viewModelScope.launch {
            _events.send(ScannerEvent.NavigateToResult(packets))
        }
    }

    /**
     * Schedules a reconnection attempt with exponential backoff.
     * Delays: 1s → 2s → 4s, capped at [MAX_RETRY_DELAY_MS].
     * Gives up after [MAX_RETRY_ATTEMPTS] and shows the manual Retry button.
     */
    private fun scheduleRetry() {
        val attempt = _uiState.value.retryAttempt
        if (attempt >= MAX_RETRY_ATTEMPTS) {
            _uiState.update { it.copy(isAutoRetrying = false) }
            return
        }
        autoRetryJob?.cancel()
        autoRetryJob = viewModelScope.launch {
            val delayMs = (INITIAL_RETRY_DELAY_MS * BACKOFF_FACTOR.pow(attempt)).toLong()
                .coerceAtMost(MAX_RETRY_DELAY_MS)
            delay(delayMs)
            _uiState.update { it.copy(retryAttempt = attempt + 1) }
            disconnectScanner()
            connectScanner()
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRetryJob?.cancel()
        disconnectScanner()
    }

    sealed interface ScannerEvent {
        data class NavigateToResult(val packets: List<BiometricReading>) : ScannerEvent
    }

    companion object {
        const val REQUIRED_CONSECUTIVE_PACKETS = 5
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 16000L
        private const val BACKOFF_FACTOR = 2.0
    }
}
