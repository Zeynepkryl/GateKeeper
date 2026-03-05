package com.zeynep.gatekeeper.navigation

import androidx.navigation3.runtime.NavKey
import com.zeynep.gatekeeper.domain.model.BiometricReading
import kotlinx.serialization.Serializable

@Serializable
sealed interface GateKeeperScreen : NavKey {

    @Serializable
    data object Scanner : GateKeeperScreen

    @Serializable
    data class Result(val packets: List<BiometricReading>) : GateKeeperScreen
}
