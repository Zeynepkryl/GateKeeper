package com.zeynep.gatekeeper.presentation.component

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zeynep.gatekeeper.R
import com.zeynep.gatekeeper.domain.model.ConnectionState
import com.zeynep.gatekeeper.ui.theme.GateKeeperTheme

@Composable
fun GKStatusIndicator(
    state: ConnectionState,
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    showLabel: Boolean = true,
) {
    val colors = GateKeeperTheme.colors
    val dotColor = when (state) {
        ConnectionState.Ready -> colors.statusReady
        ConnectionState.Error -> colors.statusError
        ConnectionState.Connecting -> colors.statusConnecting
        ConnectionState.Disconnected -> colors.statusDisconnected
    }
    val isPulsing = state == ConnectionState.Connecting

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(dotSize * 2f),
        ) {
            if (isPulsing) {
                val transition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOut),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "pulseScale",
                )
                val pulseAlpha by transition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOut),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "pulseAlpha",
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            GKText(
                text = state.toDisplayString(),
                style = GateKeeperTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ConnectionState.toDisplayString(): String = when (this) {
    ConnectionState.Disconnected -> stringResource(R.string.status_disconnected)
    ConnectionState.Connecting -> stringResource(R.string.status_connecting)
    ConnectionState.Ready -> stringResource(R.string.status_ready)
    ConnectionState.Error -> stringResource(R.string.status_error)
}
