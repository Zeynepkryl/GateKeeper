package com.zeynep.gatekeeper.data.source

import com.zeynep.gatekeeper.sdk.HardwareListener

interface BiometricSdkWrapper {
    fun addListener(listener: HardwareListener)
    fun removeListener(listener: HardwareListener)
    fun initializeAndConnect()
    fun disconnect()
}
