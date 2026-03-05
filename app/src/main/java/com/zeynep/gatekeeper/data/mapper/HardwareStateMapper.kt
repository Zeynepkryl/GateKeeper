package com.zeynep.gatekeeper.data.mapper

import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.sdk.HardwareState

object HardwareStateMapper {

    fun toDomain(sdkState: HardwareState): ConnectionState = when (sdkState) {
        HardwareState.DISCONNECTED -> ConnectionState.Disconnected
        HardwareState.BUSY -> ConnectionState.Connecting
        HardwareState.READY -> ConnectionState.Ready
        HardwareState.ERROR -> ConnectionState.Error
    }
}
