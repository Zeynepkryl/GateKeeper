package com.zeynep.gatekeeper.domain.usecase

import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.domain.repository.BiometricRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveConnectionStateUseCase @Inject constructor(
    private val repository: BiometricRepository
) {
    operator fun invoke(): StateFlow<ConnectionState> = repository.connectionState
}
