package com.zeynep.gatekeeper.domain.usecase

import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import javax.inject.Inject

class DisconnectScannerUseCase @Inject constructor(
    private val repository: BiometricRepository
) {
    operator fun invoke() = repository.disconnect()
}
