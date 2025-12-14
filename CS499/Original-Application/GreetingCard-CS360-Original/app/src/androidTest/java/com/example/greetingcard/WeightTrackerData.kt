package com.example.greetingcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.ui.theme.GreetingCardTheme

data class WeightEntry(val date: String, val weight: String)

@Composable
fun WeightTrackerData(
    entries: MutableList<WeightEntry> = mutableListOf(),
    addEntry: ((String) -> Unit)? = null,
    deleteEntry: ((Int) -> Unit)? = null
) {
    val isInPreview = LocalInspectionMode.current
    var weightInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Weight Entry", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (weightInput.isNotBlank()) {
                    if (isInPreview) {
                        // TODO: Add entry logic for preview (currently mocked)
                    } else {
                        addEntry?.invoke(weightInput)
                        weightInput = ""
                    }
                }
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Weight History", fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date", fontSize = 16.sp)
            Text("Weight", fontSize = 16.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Box {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(entries) { index, entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date: ${entry.date}", fontSize = 14.sp)
                            Text("Weight: ${entry.weight} lbs", fontSize = 16.sp)
                            IconButton(
                                onClick = {
                                    if (isInPreview) {
                                        // TODO: Handle deletion in preview (mock only)
                                    } else {
                                        deleteEntry?.invoke(index)
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.LightGray)
    )
}

@Preview(showSystemUi = true)
@Composable
fun WeightTrackerDataPreview() {
    val sampleEntries = remember {
        mutableStateListOf(
            WeightEntry("2025-04-01", "160"),
            WeightEntry("2025-04-02", "158"),
            WeightEntry("2025-04-03", "157")
        )
    }

    GreetingCardTheme {
        WeightTrackerData(
            entries = sampleEntries,
            addEntry = { weight ->
                sampleEntries.add(WeightEntry("2025-04-04", weight))
            },
            deleteEntry = { index ->
                if (index in sampleEntries.indices) sampleEntries.removeAt(index)
            }
        )
    }
}
