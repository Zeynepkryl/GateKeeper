package com.zeynep.gatekeeper.domain.usecase

import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import javax.inject.Inject

/** Safely disconnects from the biometric sensor and cleans up SDK listeners. */
class DisconnectScannerUseCase @Inject constructor(
    private val repository: BiometricRepository
) {
    operator fun invoke() = repository.disconnect()
}
