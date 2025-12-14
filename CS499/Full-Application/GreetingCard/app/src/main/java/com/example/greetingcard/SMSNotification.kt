package com.example.greetingcard

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.ui.theme.GreetingCardTheme

private const val PREFS_NAME = "weight_prefs"
private const val KEY_ALERTS_ENABLED = "alerts_enabled"
private const val KEY_ALERT_MESSAGE = "alert_message"

@Composable
fun SMSNotification() {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current

    val prefs = remember {
        if (!isInPreview) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } else {
            null
        }
    }

    var alertsEnabled by remember {
        mutableStateOf(
            prefs?.getBoolean(KEY_ALERTS_ENABLED, false) ?: false
        )
    }

    // Phone number field is empty for new input.
    var phoneNumber by remember { mutableStateOf("") }

    var message by remember {
        mutableStateOf(
            prefs?.getString(
                KEY_ALERT_MESSAGE,
                "Congrats! You reached your weight goal in Revolv 360!"
            ) ?: "Congrats! You reached your weight goal in Revolv 360!"
        )
    }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.celebrate),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Centered widget
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Alerts Enrollment",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Turn on alerts so you get a motivational message when you reach your goal weight.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Toggle for enabling/disabling alerts
                RowWithSwitch(
                    title = "Enable goal alerts",
                    checked = alertsEnabled,
                    onCheckedChange = { alertsEnabled = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number for Alerts") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    enabled = alertsEnabled
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Alert Message") },
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    enabled = alertsEnabled
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (isInPreview) {
                            isSuccess = false
                            statusMessage = "Preview mode: preferences not saved."
                            return@Button
                        }

                        // Validation only if alerts are enabled
                        if (alertsEnabled && phoneNumber.isBlank()) {
                            isSuccess = false
                            statusMessage =
                                "Please enter a phone number for alerts or turn alerts off."
                            return@Button
                        }

                        prefs?.edit()?.apply {
                            putBoolean(KEY_ALERTS_ENABLED, alertsEnabled)
                            putString(KEY_ALERT_MESSAGE, message)
                            apply()
                        }

                        isSuccess = true
                        statusMessage = if (alertsEnabled) {
                            "You are enrolled in goal alerts."
                        } else {
                            "Goal alerts turned off."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0),
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Alert Settings")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shows a success or error message after pressing "Save"
                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = if (isSuccess) {
                            Color(0xFF2E7D32)
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RowWithSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF9C27B0)
            )
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun SMSNotificationPreview() {
    GreetingCardTheme {
        SMSNotification()
    }
}
