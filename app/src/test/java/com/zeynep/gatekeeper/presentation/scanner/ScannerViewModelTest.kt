package com.zeynep.gatekeeper.presentation.scanner

import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.domain.usecase.ConnectScannerUseCase
import com.zeynep.gatekeeper.domain.usecase.DisconnectScannerUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveBiometricStreamUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveConnectionStateUseCase
import com.zeynep.gatekeeper.domain.usecase.ObserveErrorsUseCase
import com.zeynep.gatekeeper.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val connectionStateFlow = MutableStateFlow(ConnectionState.Disconnected)
    private val biometricStreamFlow = MutableSharedFlow<BiometricReading>()
    private val errorsFlow = MutableSharedFlow<String>()

    private val connectScanner: ConnectScannerUseCase = mockk(relaxed = true)
    private val disconnectScanner: DisconnectScannerUseCase = mockk(relaxed = true)

    private val observeConnectionState: ObserveConnectionStateUseCase = mockk()
    private val observeBiometricStream: ObserveBiometricStreamUseCase = mockk()
    private val observeErrors: ObserveErrorsUseCase = mockk()

    private lateinit var viewModel: ScannerViewModel

    @Before
    fun setup() {
        every { observeConnectionState.invoke() } returns connectionStateFlow
        every { observeBiometricStream.invoke() } returns biometricStreamFlow
        every { observeErrors.invoke() } returns errorsFlow

        viewModel = ScannerViewModel(
            connectScanner = connectScanner,
            disconnectScanner = disconnectScanner,
            observeBiometricStream = observeBiometricStream,
            observeConnectionState = observeConnectionState,
            observeErrors = observeErrors
        )
    }

    @Test
    fun `initial state is idle and disconnected`() {
        val state = viewModel.uiState.value
        assertEquals(ConnectionState.Disconnected, state.connectionState)
        assertEquals(0, state.consecutiveCount)
        assertTrue(state.receivedPackets.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `connection state changes are reflected in ui state`() = runTest {
        connectionStateFlow.value = ConnectionState.Connecting
        assertEquals(ConnectionState.Connecting, viewModel.uiState.value.connectionState)

        connectionStateFlow.value = ConnectionState.Ready
        assertEquals(ConnectionState.Ready, viewModel.uiState.value.connectionState)
    }

    @Test
    fun `startScan calls connect use case`() = runTest {
        coEvery { connectScanner() } returns Unit
        viewModel.startScan()
        io.mockk.coVerify { connectScanner() }
    }

    @Test
    fun `received packets increment consecutive count`() = runTest {
        connectionStateFlow.value = ConnectionState.Ready

        biometricStreamFlow.emit(BiometricReading("DATA_1", 1L))
        assertEquals(1, viewModel.uiState.value.consecutiveCount)

        biometricStreamFlow.emit(BiometricReading("DATA_2", 2L))
        assertEquals(2, viewModel.uiState.value.consecutiveCount)
    }

    @Test
    fun `five consecutive packets trigger navigation event`() = runTest {
        connectionStateFlow.value = ConnectionState.Ready

        val events = mutableListOf<ScannerViewModel.ScannerEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        repeat(5) { i ->
            biometricStreamFlow.emit(BiometricReading("BIO_DATA_$i", i.toLong()))
        }

        assertEquals(1, events.size)
        val navEvent = events.first() as ScannerViewModel.ScannerEvent.NavigateToResult
        assertEquals(5, navEvent.packets.size)

        job.cancel()
    }

    @Test
    fun `navigation event disconnects hardware and resets state`() = runTest {
        connectionStateFlow.value = ConnectionState.Ready

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect {}
        }

        repeat(5) { i ->
            biometricStreamFlow.emit(BiometricReading("BIO_DATA_$i", i.toLong()))
        }

        verify { disconnectScanner() }
        assertEquals(0, viewModel.uiState.value.consecutiveCount)
        assertTrue(viewModel.uiState.value.receivedPackets.isEmpty())

        job.cancel()
    }

    @Test
    fun `error state resets consecutive count`() = runTest {
        connectionStateFlow.value = ConnectionState.Ready

        repeat(3) { i ->
            biometricStreamFlow.emit(BiometricReading("DATA_$i", i.toLong()))
        }
        assertEquals(3, viewModel.uiState.value.consecutiveCount)

        connectionStateFlow.value = ConnectionState.Error
        assertEquals(0, viewModel.uiState.value.consecutiveCount)
    }

    @Test
    fun `error message is displayed from errors flow`() = runTest {
        errorsFlow.emit("USB cable disconnected")
        assertEquals("USB cable disconnected", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retry calls connect again`() = runTest {
        coEvery { connectScanner() } returns Unit
        viewModel.retry()
        io.mockk.coVerify { connectScanner() }
    }


    @Test
    fun `error message is cleared when connection recovers`() = runTest {
        errorsFlow.emit("Sensor disconnected unexpectedly")
        assertEquals("Sensor disconnected unexpectedly", viewModel.uiState.value.errorMessage)

        connectionStateFlow.value = ConnectionState.Ready
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `startScanWithAutoRetry preserves auto-retry flag`() = runTest {
        coEvery { connectScanner() } returns Unit
        viewModel.startScanWithAutoRetry()
        assertTrue(viewModel.uiState.value.isAutoRetrying)
    }

    @Test
    fun `startScan does not set auto-retry flag`() = runTest {
        coEvery { connectScanner() } returns Unit
        viewModel.startScan()
        assertFalse(viewModel.uiState.value.isAutoRetrying)
    }

    @Test
    fun `performScan disconnects before connecting`() = runTest {
        coEvery { connectScanner() } returns Unit
        viewModel.startScan()

        io.mockk.verifyOrder {
            disconnectScanner()

        }
    }

    @Test
    fun `navigation event is exactly-once even with extra packets`() = runTest {
        connectionStateFlow.value = ConnectionState.Ready

        val events = mutableListOf<ScannerViewModel.ScannerEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }

        repeat(5) { i ->
            biometricStreamFlow.emit(BiometricReading("BIO_DATA_$i", i.toLong()))
        }

        assertEquals(1, events.size)

        job.cancel()
    }
}
