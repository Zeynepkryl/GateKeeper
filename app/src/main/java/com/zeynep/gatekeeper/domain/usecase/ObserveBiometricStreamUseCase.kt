package com.zeynep.gatekeeper.domain.usecase

import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/** Provides a reactive stream of [BiometricReading] packets from the sensor. */
class ObserveBiometricStreamUseCase @Inject constructor(
    private val repository: BiometricRepository
) {
    operator fun invoke(): SharedFlow<BiometricReading> = repository.biometricStream
}
