package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun QuickActions(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction(
            "Ask AI",
            Icons.Default.AutoAwesome,
            "Tell me something useful"
        ),
        QuickAction(
            "Search",
            Icons.Default.Search,
            "Search the web for "
        ),
        QuickAction(
            "Memory",
            Icons.Default.Memory,
            "What do you remember about me?"
        ),
        QuickAction(
            "Web",
            Icons.Default.Language,
            "Help me research "
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                action = actions[0],
                modifier = Modifier.weight(1f),
                onClick = onAction
            )

            QuickActionCard(
                action = actions[1],
                modifier = Modifier.weight(1f),
                onClick = onAction
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                action = actions[2],
                modifier = Modifier.weight(1f),
                onClick = onAction
            )

            QuickActionCard(
                action = actions[3],
                modifier = Modifier.weight(1f),
                onClick = onAction
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = ChintuColors.Surface,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                onClick(action.prompt)
            }
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = action.icon,
            contentDescription = action.title,
            tint = ChintuColors.Accent
        )

        Text(
            text = action.title,
            color = ChintuColors.TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
