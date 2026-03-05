package com.zeynep.gatekeeper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BiometricReading(
    val rawData: String,
    val timestamp: Long
)
