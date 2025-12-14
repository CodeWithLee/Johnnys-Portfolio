package com.example.greetingcard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.ui.theme.GreetingCardTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Stores one weight record (date + weight) and an optional note for that day.
data class WeightEntry(
    val date: String,
    var weight: String,
    var notes: String = ""
)

// Used to generate/display dates in a consistent format (yyyy-MM-dd).
private val weightDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
fun WeightTrackerData(
    entries: List<WeightEntry> = emptyList(),
    addEntry: ((String, String) -> Unit)? = null,          // weight, notes
    deleteEntry: ((Int) -> Unit)? = null,
    updateEntry: ((Int, String, String) -> Unit)? = null,
    isInPreview: Boolean = false
) {
    // Inputs for adding a new entry
    var weightInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    // Editing an existing entry in the history list
    var editingIndex by remember { mutableStateOf(-1) }
    var editingWeight by remember { mutableStateOf("") }
    var editingNotes by remember { mutableStateOf("") }

    // Preview mode to the screen can be tested.
    val previewEntries = remember { mutableStateListOf<WeightEntry>() }
    val displayedEntries = if (isInPreview) previewEntries else entries

    // Stores recent changes.
    var recentChangeOverride by remember { mutableStateOf<Double?>(null) }
    val calculatedChange = calculateRecentChange(displayedEntries)
    val recentChange = recentChangeOverride ?: calculatedChange

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Background image for the weight tracking screen
        Image(
            painter = painterResource(id = R.drawable.scale),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.85f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Weight change badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Weight Overview",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeightChangeBadge(recentChange = recentChange)
            }

            // Card for adding a new weight entry + notes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add Today’s Weight",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "Log your weight once a day and add a quick note for extra context.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (lbs)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (weightInput.isNotBlank()) {
                                    // Update the badge immediately by comparing the new weight
                                    // to the most recent saved weight (if one exists).
                                    val previousWeight =
                                        displayedEntries.lastOrNull()?.weight?.toDoubleOrNull()
                                    val newWeight = weightInput.toDoubleOrNull()
                                    if (previousWeight != null && newWeight != null) {
                                        recentChangeOverride = newWeight - previousWeight
                                    }

                                    if (isInPreview) {
                                        val currentDate = weightDateFormat.format(Date())
                                        previewEntries.add(
                                            WeightEntry(
                                                date = currentDate,
                                                weight = weightInput,
                                                notes = noteInput
                                            )
                                        )
                                    } else {
                                        addEntry?.invoke(weightInput, noteInput)
                                    }
                                    weightInput = ""
                                    noteInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Add a note") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            // History list shows saved entries and allows edit/delete actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Weight History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Tap to edit or delete",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .background(Color.Transparent)
                )

                displayedEntries.forEachIndexed { index, entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (editingIndex == index) {
                                OutlinedTextField(
                                    value = editingWeight,
                                    onValueChange = { editingWeight = it },
                                    label = { Text("Edit Weight (lbs)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = editingNotes,
                                    onValueChange = { editingNotes = it },
                                    label = { Text("Edit Note (optional)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            // When saving an edit, populate the immediate change so the badge updates.
                                            val previousWeight =
                                                displayedEntries.getOrNull(index)?.weight?.toDoubleOrNull()
                                            val newWeight = editingWeight.toDoubleOrNull()
                                            if (previousWeight != null && newWeight != null) {
                                                recentChangeOverride = newWeight - previousWeight
                                            }

                                            if (isInPreview) {
                                                val current = previewEntries[index]
                                                previewEntries[index] = current.copy(
                                                    weight = editingWeight,
                                                    notes = editingNotes
                                                )
                                            } else {
                                                updateEntry?.invoke(
                                                    index,
                                                    editingWeight,
                                                    editingNotes
                                                )
                                            }
                                            editingIndex = -1
                                            editingWeight = ""
                                            editingNotes = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF9C27B0),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Save")
                                    }
                                }
                            } else {
                                Text(
                                    "Date: ${entry.date}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Weight: ${entry.weight} lbs",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            editingIndex = index
                                            editingWeight = entry.weight
                                            editingNotes = entry.notes
                                        }) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = {
                                            if (isInPreview) {
                                                previewEntries.removeAt(index)
                                            } else {
                                                deleteEntry?.invoke(index)
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFD32F2F)
                                            )
                                        }
                                    }
                                }

                                if (entry.notes.isNotBlank()) {
                                    Text(
                                        "Note",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        entry.notes,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Divider used between sections in the weight screen
@Composable
fun HorizontalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    )
}

/**
 * Badge showing change since the last entry or last update.
 * Positive = gained, Negative = lost.
 */
@Composable
private fun WeightChangeBadge(recentChange: Double?) {
    val (label, subtext, backgroundColor, textColor) = when {
        recentChange == null -> {
            Quadruple(
                "Keep logging",
                "Log at least one more weight to see your recent change.",
                MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                Color.White
            )
        }

        recentChange < 0 -> {
            val loss = abs(recentChange)
            Quadruple(
                String.format(Locale.getDefault(), "-%.1f lbs", loss),
                "Lost since your last entry. Nice work!",
                Color(0xFF4CAF50).copy(alpha = 0.95f),
                Color.White
            )
        }

        recentChange > 0 -> {
            Quadruple(
                String.format(Locale.getDefault(), "+%.1f lbs", recentChange),
                "Gained since your last entry. Stay consistent.",
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.95f),
                Color.White
            )
        }

        else -> {
            Quadruple(
                "0.0 lbs",
                "No change since your last entry.",
                Color(0xFF9E9E9E).copy(alpha = 0.95f),
                Color.White
            )
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = subtext,
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Fallback: calculate change between the last two entries (if more than one exists).
 * Positive = gained, Negative = lost.
 */
private fun calculateRecentChange(entries: List<WeightEntry>): Double? {
    if (entries.size < 2) return null

    val latestWeight = entries.last().weight.toDoubleOrNull()
    val previousWeight = entries[entries.size - 2].weight.toDoubleOrNull()

    if (latestWeight == null || previousWeight == null) return null

    return latestWeight - previousWeight
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WeightTrackerDataPreview() {
    GreetingCardTheme {
        WeightTrackerData(isInPreview = true)
    }
}
