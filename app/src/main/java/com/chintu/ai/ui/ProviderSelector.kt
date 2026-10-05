package com.chintu.ai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chintu.ai.ai.ProviderCatalog

@Composable
fun ProviderSelector(
    selectedProvider: String,
    onProviderSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedName =
        ProviderCatalog.name(
            selectedProvider
        )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text("AI Provider")

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            ProviderCatalog.all.forEach { provider ->

                DropdownMenuItem(
                    text = {
                        Text(provider.name)
                    },
                    onClick = {

                        onProviderSelected(
                            provider.id
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}
