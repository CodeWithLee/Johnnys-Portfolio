package com.example.greetingcard

import android.Manifest
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.greetingcard.ui.theme.GreetingCardTheme

@Composable
fun SMSNotification() {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        statusMessage = if (granted) {
            "SMS permission granted."
        } else {
            "Permission denied. Cannot send SMS."
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ Background image (guarded for preview)
        if (!isPreview) {
            Image(
                painter = painterResource(id = R.drawable.celebrate),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Stay Motivated With Alerts", fontSize = 24.sp, color = Color.Green)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                singleLine = false,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.SEND_SMS)
                    } else {
                        // ✅ Attempt to send SMS
                        try {
                            val smsManager = SmsManager.getDefault()
                            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                            statusMessage = "SMS sent successfully!"
                        } catch (e: Exception) {
                            statusMessage = "Error sending SMS: ${e.localizedMessage}"
                            // TODO: Handle failure more thoroughly
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("Send SMS")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage, color = Color.White)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SMSNotificationPreview() {
    GreetingCardTheme {
        SMSNotification()
    }
}
