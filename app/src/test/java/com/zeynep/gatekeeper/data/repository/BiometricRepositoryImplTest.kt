package com.zeynep.gatekeeper.data.repository

import app.cash.turbine.test
import com.zeynep.gatekeeper.data.source.FakeBiometricSdkWrapper
import com.zeynep.gatekeeper.domain.model.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricRepositoryImplTest {

    private lateinit var fakeSdk: FakeBiometricSdkWrapper
    private lateinit var repository: BiometricRepositoryImpl
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        fakeSdk = FakeBiometricSdkWrapper()
        repository = BiometricRepositoryImpl(fakeSdk, testDispatcher)
    }

    @Test
    fun `initial state is Disconnected`() {
        assertEquals(ConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `connect success results in Ready state`() = runTest(testDispatcher) {
        repository.connect()
        assertEquals(ConnectionState.Ready, repository.connectionState.value)
    }

    @Test
    fun `connect failure results in Error state`() = runTest(testDispatcher) {
        fakeSdk.shouldFailOnConnect = true
        fakeSdk.errorMessage = "USB Handshake Failed"

        repository.connect()
        assertEquals(ConnectionState.Error, repository.connectionState.value)
    }

    @Test
    fun `connect failure emits error message`() = runTest(testDispatcher) {
        fakeSdk.shouldFailOnConnect = true
        fakeSdk.errorMessage = "USB Handshake Failed"

        repository.errors.test {
            launch { repository.connect() }

            assertEquals("USB Handshake Failed", awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `disconnect sets state to Disconnected`() = runTest(testDispatcher) {
        repository.connect()
        repository.disconnect()

        assertEquals(ConnectionState.Disconnected, repository.connectionState.value)
    }

    @Test
    fun `data received is emitted to biometric stream`() = runTest(testDispatcher) {
        repository.biometricStream.test {
            repository.connect()

            fakeSdk.simulateDataReceived("BIO_DATA_1000")

            val reading = awaitItem()
            assertEquals("BIO_DATA_1000", reading.rawData)
            assertEquals(1000L, reading.timestamp)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `listener is deactivated after disconnect`() = runTest(testDispatcher) {
        repository.connect()
        repository.disconnect()

        repository.biometricStream.test {
            fakeSdk.simulateDataReceived("BIO_DATA_AFTER_DISCONNECT")
            expectNoEvents()
        }
    }
}
