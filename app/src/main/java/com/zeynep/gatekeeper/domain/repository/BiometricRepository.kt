package com.zeynep.gatekeeper.domain.repository

import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain-level contract for biometric hardware interactions.
 *
 * Converts the callback-based legacy SDK into reactive [StateFlow] and [SharedFlow]
 * streams, ensuring thread-safe observation of connection state, biometric data,
 * and error events without leaking SDK implementation details.
 */
interface BiometricRepository {

    val connectionState: StateFlow<ConnectionState>
    val biometricStream: SharedFlow<BiometricReading>
    val errors: SharedFlow<String>
    suspend fun connect()
    fun disconnect()
}
