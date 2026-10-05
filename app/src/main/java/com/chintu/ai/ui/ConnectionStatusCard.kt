package com.chintu.ai.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionStatusCard(
    message: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 4.dp
            )
    ) {

        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}
