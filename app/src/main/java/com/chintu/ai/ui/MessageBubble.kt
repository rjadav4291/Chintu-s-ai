package com.chintu.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chintu.ai.ai.ChatMessage

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser =
        message.role.equals(
            "user",
            ignoreCase = true
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 5.dp
            ),
        horizontalArrangement =
            if (isUser) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
        verticalAlignment = Alignment.Bottom
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .background(
                    color =
                        if (isUser) {
                            ChintuColors.UserBubble
                        } else {
                            ChintuColors.AssistantBubble
                        },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart =
                            if (isUser) 18.dp else 4.dp,
                        bottomEnd =
                            if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(
                    horizontal = 15.dp,
                    vertical = 11.dp
                )
        ) {

            Text(
                text =
                    if (isUser) {
                        "YOU"
                    } else {
                        "CHINTU"
                    },
                color =
                    if (isUser) {
                        ChintuColors.Accent
                    } else {
                        ChintuColors.Primary
                    },
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message.content,
                color = ChintuColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
