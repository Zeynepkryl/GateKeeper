package com.zeynep.gatekeeper.data.source

import com.zeynep.gatekeeper.sdk.HardwareListener
import com.zeynep.gatekeeper.sdk.LegacyBiometricSdk
import javax.inject.Inject

/**
 * Production implementation that delegates directly to the
 * [LegacyBiometricSdk] singleton. This thin wrapper exists solely
 * to decouple the data layer from the concrete SDK object,
 * allowing [FakeBiometricSdkWrapper][com.zeynep.gatekeeper.data.source.FakeBiometricSdkWrapper]
 * to be substituted in tests.
 */
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
