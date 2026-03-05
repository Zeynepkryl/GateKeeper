package com.zeynep.gatekeeper.domain.usecase

import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import javax.inject.Inject

class ConnectScannerUseCase @Inject constructor(
    private val repository: BiometricRepository
) {
    suspend operator fun invoke() = repository.connect()
}
