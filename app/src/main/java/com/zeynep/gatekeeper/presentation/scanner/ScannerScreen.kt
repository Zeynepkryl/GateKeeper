package com.zeynep.gatekeeper.presentation.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.presentation.component.GKCard
import com.zeynep.gatekeeper.presentation.component.GKOutlinedButton
import com.zeynep.gatekeeper.presentation.component.GKPrimaryButton
import com.zeynep.gatekeeper.presentation.component.GKStatusIndicator
import com.zeynep.gatekeeper.presentation.component.GKText
import com.zeynep.gatekeeper.presentation.scanner.ScannerViewModel.Companion.REQUIRED_CONSECUTIVE_PACKETS
import com.zeynep.gatekeeper.ui.theme.GateKeeperTheme
import com.zeynep.gatekeeper.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    uiState: ScannerUiState,
    onStartScan: () -> Unit,
    onRetry: () -> Unit,
    onStartAutoRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = GateKeeperTheme.spacing

    Scaffold(
        containerColor = GateKeeperTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        GKText(
                            text = "Gate Entry Scanner",
                            style = GateKeeperTheme.typography.titleLarge,
                            color = GateKeeperTheme.colors.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GateKeeperTheme.colors.primary,
                    titleContentColor = GateKeeperTheme.colors.onPrimary,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            StatusSection(uiState)
            ActionButtons(uiState, onStartScan, onRetry, onStartAutoRetry)

            AnimatedVisibility(
                visible = uiState.isReady || uiState.consecutiveCount > 0,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
            ) {
                ProgressSection(uiState.consecutiveCount)
            }

            if (uiState.receivedPackets.isNotEmpty()) {
                PacketList(
                    packets = uiState.receivedPackets,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusSection(uiState: ScannerUiState) {
    val containerColor = when (uiState.connectionState) {
        ConnectionState.Ready -> GateKeeperTheme.colors.primaryContainer
        ConnectionState.Error -> GateKeeperTheme.colors.errorContainer
        ConnectionState.Connecting -> GateKeeperTheme.colors.surfaceVariant
        ConnectionState.Disconnected -> GateKeeperTheme.colors.surface
    }

    GKCard(
        containerColor = containerColor,
        modifier = Modifier.animateContentSize(),
    ) {
        GKStatusIndicator(state = uiState.connectionState)

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(GateKeeperTheme.spacing.sm))
            GKText(
                text = uiState.errorMessage,
                color = GateKeeperTheme.colors.error,
                style = GateKeeperTheme.typography.bodyMedium,
            )
        }

        if (uiState.isAutoRetrying) {
            Spacer(modifier = Modifier.height(GateKeeperTheme.spacing.sm))
            GKText(
                text = "Auto-retrying... (attempt ${uiState.retryAttempt + 1})",
                style = GateKeeperTheme.typography.bodySmall,
                color = GateKeeperTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ActionButtons(
    uiState: ScannerUiState,
    onStartScan: () -> Unit,
    onRetry: () -> Unit,
    onStartAutoRetry: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GateKeeperTheme.spacing.sm),
    ) {
        when {
            uiState.hasError && !uiState.isAutoRetrying -> {
                GKPrimaryButton(
                    text = "Retry",
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.Refresh,
                )
                GKOutlinedButton(
                    text = "Auto-Retry",
                    onClick = onStartAutoRetry,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.Autorenew,
                )
            }

            uiState.canStartScan -> {
                GKPrimaryButton(
                    text = "Start Scanner",
                    onClick = onStartScan,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Outlined.PlayArrow,
                )
            }

            uiState.isConnecting -> {
                GKPrimaryButton(
                    text = "Connecting...",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = true,
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(consecutiveCount: Int) {
    GKCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GKText(
                text = "Consecutive Packets",
                style = GateKeeperTheme.typography.bodyMedium,
            )
            GKText(
                text = "$consecutiveCount / $REQUIRED_CONSECUTIVE_PACKETS",
                style = GateKeeperTheme.typography.titleSmall,
                color = GateKeeperTheme.colors.secondary,
            )
        }
        Spacer(modifier = Modifier.height(GateKeeperTheme.spacing.sm))
        LinearProgressIndicator(
            progress = { consecutiveCount.toFloat() / REQUIRED_CONSECUTIVE_PACKETS },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp)),
            color = GateKeeperTheme.colors.secondary,
            trackColor = GateKeeperTheme.colors.surfaceVariant,
        )
    }
}

@Composable
private fun PacketList(
    packets: List<BiometricReading>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(packets.size) {
        if (packets.isNotEmpty()) {
            listState.animateScrollToItem(packets.lastIndex)
        }
    }

    Column(modifier = modifier) {
        GKText(
            text = "Received Data",
            style = GateKeeperTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = GateKeeperTheme.spacing.xs),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(GateKeeperTheme.spacing.xs),
        ) {
            itemsIndexed(items = packets, key = { index, _ -> index }) { _, packet ->
                GKCard(contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GKText(
                            text = packet.rawData,
                            style = GateKeeperTheme.typography.mono,
                            modifier = Modifier.weight(1f),
                        )
                        GKText(
                            text = formatTimestamp(packet.timestamp),
                            style = GateKeeperTheme.typography.labelSmall,
                            color = GateKeeperTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String = DateFormatter.formatTime(timestamp)
