package com.zeynep.gatekeeper.domain.repository

import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BiometricRepository {
    val connectionState: StateFlow<ConnectionState>
    val biometricStream: SharedFlow<BiometricReading>
    val errors: SharedFlow<String>

    suspend fun connect()
    fun disconnect()
}
