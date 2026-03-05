package com.zeynep.gatekeeper.data.source

import com.zeynep.gatekeeper.sdk.HardwareListener
import com.zeynep.gatekeeper.sdk.LegacyBiometricSdk
import javax.inject.Inject

class DefaultBiometricSdkWrapper @Inject constructor() : BiometricSdkWrapper {

    override fun addListener(listener: HardwareListener) =
        LegacyBiometricSdk.addListener(listener)

    override fun removeListener(listener: HardwareListener) =
        LegacyBiometricSdk.removeListener(listener)

    override fun initializeAndConnect() =
        LegacyBiometricSdk.initializeAndConnect()

    override fun disconnect() =
        LegacyBiometricSdk.disconnect()
}
