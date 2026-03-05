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

    fun startScan() {
        autoRetryJob?.cancel()
        _uiState.update {
            it.copy(
                errorMessage = null,
                consecutiveCount = 0,
                receivedPackets = emptyList(),
                isAutoRetrying = false,
                retryAttempt = 0
            )
        }
        viewModelScope.launch { connectScanner() }
    }

    fun retry() = startScan()

    fun startScanWithAutoRetry() {
        _uiState.update { it.copy(isAutoRetrying = true, retryAttempt = 0) }
        startScan()
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
