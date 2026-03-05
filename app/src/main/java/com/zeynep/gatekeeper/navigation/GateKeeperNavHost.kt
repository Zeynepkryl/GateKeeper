package com.zeynep.gatekeeper.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zeynep.gatekeeper.presentation.result.ResultScreen
import com.zeynep.gatekeeper.presentation.scanner.ScannerScreen
import com.zeynep.gatekeeper.presentation.scanner.ScannerViewModel

@Composable
fun GateKeeperNavHost(modifier: Modifier = Modifier) {
    val backStack = remember {
        mutableStateListOf<GateKeeperScreen>(GateKeeperScreen.Scanner)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<GateKeeperScreen.Scanner> {
                val viewModel: ScannerViewModel = hiltViewModel(
                    viewModelStoreOwner = LocalContext.current as ComponentActivity
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is ScannerViewModel.ScannerEvent.NavigateToResult -> {
                                backStack.add(
                                    GateKeeperScreen.Result(packets = event.packets)
                                )
                            }
                        }
                    }
                }

                ScannerScreen(
                    uiState = uiState,
                    onStartScan = viewModel::startScan,
                    onRetry = viewModel::retry,
                    onStartAutoRetry = viewModel::startScanWithAutoRetry
                )
            }

            entry<GateKeeperScreen.Result> { screen ->
                ResultScreen(
                    packets = screen.packets,
                    onFinish = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
