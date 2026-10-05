package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isError =
        status.contains("FAILED", ignoreCase = true) ||
        status.contains("ERROR", ignoreCase = true)

    val isThinking =
        status.contains("THINKING", ignoreCase = true)

    val color = when {
        isError -> ChintuColors.Error
        isThinking -> ChintuColors.Warning
        else -> ChintuColors.Success
    }

    Row(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "● $status",
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 4.dp,
                vertical = 8.dp
            ),
        color = ChintuColors.TextSecondary,
        fontSize = 12.sp
    )
}
