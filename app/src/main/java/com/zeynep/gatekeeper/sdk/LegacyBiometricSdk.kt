package com.zeynep.gatekeeper.sdk

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

// -----------------------------------------------------------------------
// MOCK LEGACY SDK - DO NOT MODIFY
// Treats this as an external library
// -----------------------------------------------------------------------

enum class HardwareState {
    DISCONNECTED, BUSY, READY, ERROR
}

interface HardwareListener {
    fun onStateChanged(state: HardwareState)
    fun onDataReceived(data: String)
    fun onError(message: String)
}

object LegacyBiometricSdk {

    private val listeners = CopyOnWriteArrayList<HardwareListener>()
    private var currentState = HardwareState.DISCONNECTED
    private val isStreaming = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun addListener(listener: HardwareListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: HardwareListener) {
        listeners.remove(listener)
    }

    fun initializeAndConnect() {
        updateState(HardwareState.BUSY)
        try {
            Thread.sleep(2000)
            if (Random.nextBoolean()) {
                throw RuntimeException("USB Handshake Failed")
            }
            updateState(HardwareState.READY)
            startInternalDataStream()
        } catch (e: Exception) {
            updateState(HardwareState.ERROR)
            notifyError(e.message ?: "Unknown init error")
        }
    }

    private fun startInternalDataStream() {
        isStreaming.set(true)
        Thread {
            while (isStreaming.get()) {
                try {
                    Thread.sleep(1000)
                    if (Random.nextInt(10) == 1) {
                        throw RuntimeException("Sensor disconnected unexpectedly")
                    }
                    val mockData = "BIO_DATA_${System.currentTimeMillis()}"
                    mainHandler.post {
                        listeners.forEach { it.onDataReceived(mockData) }
                    }
                } catch (e: Exception) {
                    isStreaming.set(false)
                    updateState(HardwareState.ERROR)
                    notifyError(e.message ?: "Stream error")
                    break
                }
            }
        }.start()
    }

    fun disconnect() {
        isStreaming.set(false)
        updateState(HardwareState.DISCONNECTED)
    }

    private fun updateState(newState: HardwareState) {
        currentState = newState
        mainHandler.post {
            listeners.forEach { it.onStateChanged(newState) }
        }
    }

    private fun notifyError(msg: String) {
        mainHandler.post {
            listeners.forEach { it.onError(msg) }
        }
    }
}
