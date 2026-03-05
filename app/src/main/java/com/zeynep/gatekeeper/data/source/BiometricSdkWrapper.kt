package com.zeynep.gatekeeper.data.source

import com.zeynep.gatekeeper.sdk.HardwareListener

/**
 * Abstraction layer over the legacy [com.zeynep.gatekeeper.sdk.LegacyBiometricSdk].
 *
 * Isolates the domain and data layers from the concrete SDK singleton,
 * enabling testability through fake implementations and preventing
 * direct coupling to the unstable hardware driver.
 */
interface BiometricSdkWrapper {
    fun addListener(listener: HardwareListener)
    fun removeListener(listener: HardwareListener)
    fun initializeAndConnect()
    fun disconnect()
}
