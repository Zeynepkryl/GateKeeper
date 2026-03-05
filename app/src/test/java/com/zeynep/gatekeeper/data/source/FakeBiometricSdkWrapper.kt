package com.zeynep.gatekeeper.data.source

import com.zeynep.gatekeeper.sdk.HardwareListener
import com.zeynep.gatekeeper.sdk.HardwareState

class FakeBiometricSdkWrapper : BiometricSdkWrapper {

    private val listeners = mutableListOf<HardwareListener>()

    var shouldFailOnConnect = false
    var errorMessage = "Connection failed"

    override fun addListener(listener: HardwareListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: HardwareListener) {
        listeners.remove(listener)
    }

    override fun initializeAndConnect() {
        notifyState(HardwareState.BUSY)
        if (shouldFailOnConnect) {
            notifyState(HardwareState.ERROR)
            notifyError(errorMessage)
        } else {
            notifyState(HardwareState.READY)
        }
    }

    override fun disconnect() {
        notifyState(HardwareState.DISCONNECTED)
    }

    fun simulateDataReceived(data: String) {
        listeners.toList().forEach { it.onDataReceived(data) }
    }

    fun simulateError(message: String) {
        notifyState(HardwareState.ERROR)
        notifyError(message)
    }

    private fun notifyState(state: HardwareState) {
        listeners.toList().forEach { it.onStateChanged(state) }
    }

    private fun notifyError(msg: String) {
        listeners.toList().forEach { it.onError(msg) }
    }
}
