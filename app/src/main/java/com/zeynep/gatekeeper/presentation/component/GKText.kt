package com.zeynep.gatekeeper.presentation.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.zeynep.gatekeeper.ui.theme.GateKeeperTheme

@Composable
fun GKText(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = GateKeeperTheme.colors.textPrimary,
    style: TextStyle = GateKeeperTheme.typography.bodyMedium,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    textDecoration: TextDecoration? = null,
) {
    Text(
        text = text?.takeIf { it.isNotBlank() } ?: "\u00A0",
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
        softWrap = softWrap,
        textDecoration = textDecoration,
    )
}
