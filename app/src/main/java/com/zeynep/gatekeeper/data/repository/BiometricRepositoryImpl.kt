package com.zeynep.gatekeeper.data.repository

import com.zeynep.gatekeeper.data.mapper.BiometricDataMapper
import com.zeynep.gatekeeper.data.mapper.HardwareStateMapper
import com.zeynep.gatekeeper.data.source.BiometricSdkWrapper
import com.zeynep.gatekeeper.di.qualifier.IoDispatcher
import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import com.zeynep.gatekeeper.sdk.HardwareListener
import com.zeynep.gatekeeper.sdk.HardwareState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BiometricRepositoryImpl @Inject constructor(
    private val sdkWrapper: BiometricSdkWrapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BiometricRepository {

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _biometricStream = MutableSharedFlow<BiometricReading>(extraBufferCapacity = 64)
    override val biometricStream: SharedFlow<BiometricReading> = _biometricStream.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    @Volatile
    private var isListenerActive = false

    private val sdkListener = object : HardwareListener {
        override fun onStateChanged(state: HardwareState) {
            if (!isListenerActive) return
            _connectionState.value = HardwareStateMapper.toDomain(state)
        }

        override fun onDataReceived(data: String) {
            if (!isListenerActive) return
            _biometricStream.tryEmit(BiometricDataMapper.toDomain(data))
        }

        override fun onError(message: String) {
            if (!isListenerActive) return
            _errors.tryEmit(message)
        }
    }

    override suspend fun connect() {
        isListenerActive = true
        sdkWrapper.addListener(sdkListener)
        withContext(ioDispatcher) {
            sdkWrapper.initializeAndConnect()
        }
    }

    override fun disconnect() {
        isListenerActive = false
        sdkWrapper.removeListener(sdkListener)
        sdkWrapper.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }
}
