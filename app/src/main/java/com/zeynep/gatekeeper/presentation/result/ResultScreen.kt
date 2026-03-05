package com.zeynep.gatekeeper.presentation.result

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zeynep.gatekeeper.R
import com.zeynep.gatekeeper.domain.model.BiometricReading
import com.zeynep.gatekeeper.presentation.component.GKCard
import com.zeynep.gatekeeper.presentation.component.GKPrimaryButton
import com.zeynep.gatekeeper.presentation.component.GKText
import com.zeynep.gatekeeper.ui.theme.GateKeeperTheme
import com.zeynep.gatekeeper.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    packets: List<BiometricReading>,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = GateKeeperTheme.spacing

    var animationTriggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "checkScale",
    )
    LaunchedEffect(Unit) { animationTriggered = true }

    Scaffold(
        containerColor = GateKeeperTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(spacing.sm))
                        GKText(
                            text = stringResource(R.string.result_title),
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
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Spacer(modifier = Modifier.height(spacing.lg))

            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = CircleShape,
                color = GateKeeperTheme.colors.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.result_success_description),
                        modifier = Modifier.size(48.dp),
                        tint = GateKeeperTheme.colors.onPrimary,
                    )
                }
            }

            GKText(
                text = stringResource(R.string.result_verification_successful),
                style = GateKeeperTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )

            GKText(
                text = stringResource(R.string.result_packets_verified, packets.size),
                style = GateKeeperTheme.typography.bodyMedium,
                color = GateKeeperTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            GKCard {
                GKText(
                    text = stringResource(R.string.result_verified_packets),
                    style = GateKeeperTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(spacing.sm))

                packets.forEachIndexed { index, packet ->
                    PacketRow(index = index + 1, packet = packet)
                    if (index < packets.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = GateKeeperTheme.spacing.xs),
                            color = GateKeeperTheme.colors.divider,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            GKPrimaryButton(
                text = stringResource(R.string.result_finish),
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Outlined.CheckCircle,
            )
        }
    }
}

@Composable
private fun PacketRow(index: Int, packet: BiometricReading) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GateKeeperTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GKText(
            text = "$index. ${packet.rawData}",
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

private fun formatTimestamp(timestamp: Long): String = DateFormatter.formatTime(timestamp)
