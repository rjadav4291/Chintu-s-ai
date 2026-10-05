package com.chintu.ai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chintu.ai.ai.ModelCatalog
import com.chintu.ai.ai.ModelManager

@Composable
fun ModelSelector(
    provider: String,
    selectedModel: String,
    modelManager: ModelManager,
    onModelSelected: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    var customModel by remember {
        mutableStateOf("")
    }

    val savedModels =
        modelManager
            .getModelsForProvider(provider)
            .map { it.name }

    val defaultModels =
        ModelCatalog.defaultModels(provider)

    val models =
        (savedModels + defaultModels)
            .distinct()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text("AI Model")

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (selectedModel.isBlank()) {
                    "Select model"
                } else {
                    selectedModel
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            models.forEach { model ->

                DropdownMenuItem(
                    text = {
                        Text(model)
                    },
                    onClick = {

                        onModelSelected(model)
                        expanded = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = customModel,
            onValueChange = {
                customModel = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Or enter model name manually")
            },
            singleLine = true
        )

        if (customModel.isNotBlank()) {

            OutlinedButton(
                onClick = {

                    onModelSelected(
                        customModel.trim()
                    )

                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Custom Model")
            }
        }
    }
}
