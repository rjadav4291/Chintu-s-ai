package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chintu.ai.ai.AiMode

@Composable
fun ModeSelector(
    mode: AiMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        ModeChip(
            text = "AUTO",
            selected = mode == AiMode.AUTO,
            onClick = onClick
        )

        ModeChip(
            text = "ONLINE",
            selected = mode == AiMode.ONLINE,
            onClick = onClick
        )

        ModeChip(
            text = "OFFLINE",
            selected = mode == AiMode.OFFLINE,
            onClick = onClick
        )

        ModeChip(
            text = "PRIVATE",
            selected = mode == AiMode.PRIVATE,
            onClick = onClick
        )
    }
}

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (selected) {
            ChintuColors.OnPrimary
        } else {
            ChintuColors.TextSecondary
        },
        fontSize = 11.sp,
        modifier = Modifier
            .background(
                color = if (selected) {
                    ChintuColors.Primary
                } else {
                    ChintuColors.Surface
                },
                shape = RoundedCornerShape(50.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 15.dp,
                vertical = 8.dp
            )
    )
}
