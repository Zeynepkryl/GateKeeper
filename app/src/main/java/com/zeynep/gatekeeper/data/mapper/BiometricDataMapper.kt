package com.zeynep.gatekeeper.data.mapper

import com.zeynep.gatekeeper.domain.model.BiometricReading

object BiometricDataMapper {

    fun toDomain(rawData: String): BiometricReading {
        val timestamp = rawData.substringAfterLast("_").toLongOrNull()
            ?: System.currentTimeMillis()
        return BiometricReading(rawData = rawData, timestamp = timestamp)
    }
}
