package com.example.greetingcard

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/* KEYS & SCREENS */

// Keys and preference file names for saving authentication and settings
private const val PREFS_AUTH = "auth_prefs"
private const val PREFS_SETTINGS = "settings_prefs"
private const val KEY_DARK_MODE = "dark_mode"


private enum class Screen { Login, CreateAccount, Home }


private enum class HomeTab { Home, Weight, Alerts }

/* ACTIVITY */

// Main Android activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { AppRoot() }
    }
}

/* APP ROOT */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    val context = LocalContext.current

    // Tracks if the user has dark mode turned on.
    var darkMode by remember { mutableStateOf(readDarkMode(context)) }

    // Tracks which main screen is currently visible (Login, CreateAccount, or Home).
    var screen by remember { mutableStateOf(Screen.Login) }

    // Stores the username of the currently logged-in user
    var currentUser by remember { mutableStateOf<String?>(null) }

    var homeTab by remember { mutableStateOf(HomeTab.Home) }

    // Wrap all content in theme to support light and dark mode.
    AppTheme(darkTheme = darkMode) {
        Scaffold(
            topBar = {
                // App title and a dark mode toggle switch.
                TopAppBar(
                    title = { Text("Revolv 360 Weight Tracker") },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text("Dark", modifier = Modifier.padding(end = 8.dp))
                            // Switch allows user to toggle dark mode on and off.
                            Switch(
                                checked = darkMode,
                                onCheckedChange = {
                                    darkMode = it

                                    writeDarkMode(context, it)
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {

                if (screen == Screen.Home) {
                    NavigationBar {
                        // Home tab
                        NavigationBarItem(
                            selected = homeTab == HomeTab.Home,
                            onClick = { homeTab = HomeTab.Home },
                            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home") }
                        )
                        // Weight tracking tab
                        NavigationBarItem(
                            selected = homeTab == HomeTab.Weight,
                            onClick = { homeTab = HomeTab.Weight },
                            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Weight") },
                            label = { Text("Add Weight") }
                        )
                        // Alerts tab (SMS notification screen)
                        NavigationBarItem(
                            selected = homeTab == HomeTab.Alerts,
                            onClick = { homeTab = HomeTab.Alerts },
                            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Alerts") },
                            label = { Text("Alerts") }
                        )
                    }
                }
            }
        ) { innerPadding ->

            Box(Modifier.padding(innerPadding)) {
                when (screen) {
                    // Login screen is the first screen the user sees.
                    Screen.Login -> LoginScreen(
                        onLogin = { u, p ->
                            // Validate login credentials
                            val ok = validateLogin(context, u, p)
                            if (ok) {
                                currentUser = u
                                Toast.makeText(context, "Welcome, $u!", Toast.LENGTH_SHORT).show()

                                screen = Screen.Home
                                homeTab = HomeTab.Home
                            } else {
                                Toast.makeText(
                                    context,
                                    "Invalid username or password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onCreateAccountClick = { screen = Screen.CreateAccount }
                    )

                    // New account creation.
                    Screen.CreateAccount -> CreateAccountScreen(
                        onCreate = { u, p ->

                            val (success, msg) = createAccount(context, u, p)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) screen = Screen.Login
                        },
                        onBackToLogin = { screen = Screen.Login }
                    )


                    Screen.Home -> {
                        when (homeTab) {
                            HomeTab.Home -> HomeScreen(
                                username = currentUser ?: "User",
                                onLogout = {
                                    currentUser = null
                                    screen = Screen.Login
                                }
                            )

                            // Tab where the user can enter and view weight history.
                            HomeTab.Weight -> WeightHomeTab()

                            // Tab that shows the SMS notification.
                            HomeTab.Alerts -> SMSNotification()
                        }
                    }
                }
            }
        }
    }
}

/* HOME: WEIGHT TAB CONTENT  */

@Composable
private fun WeightHomeTab() {

    val entries = remember { mutableStateListOf<WeightEntry>() }

    // Formatter used to label each entry with the current date (yyyy-MM-dd).
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    WeightTrackerData(
        entries = entries,
        addEntry = { weight, notes ->
            val date = formatter.format(Date())

            // Check if an entry already exists for today's date.
            // If it does, we update that entry instead of adding a duplicate.
            val existingIndex = entries.indexOfFirst { it.date == date }
            if (existingIndex >= 0) {
                val current = entries[existingIndex]
                entries[existingIndex] = current.copy(
                    weight = weight,
                    notes = notes
                )
            } else {
                // If today does not have an entry yet, creates a new one at the top of the list.
                entries.add(0, WeightEntry(date = date, weight = weight, notes = notes))
            }
        },
        deleteEntry = { index ->
            // Remove an entry when the user taps delete.
            if (index in entries.indices) {
                entries.removeAt(index)
            }
        },
        updateEntry = { index, newWeight, newNotes ->
            // Update an existing entry.
            if (index in entries.indices) {
                val current = entries[index]
                entries[index] = current.copy(
                    weight = newWeight,
                    notes = newNotes
                )
            }
        }
    )
}

/* THEME */

@Composable
private fun AppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val scheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = scheme, content = content)
}

/* SETTINGS PERSISTENCE */

private fun readDarkMode(context: Context): Boolean =
    context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        .getBoolean(KEY_DARK_MODE, false)

// Remember dark mode setting when user sign on again.
private fun writeDarkMode(context: Context, value: Boolean) {
    context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_DARK_MODE, value)
        .apply()
}

/* AUTH (LOCAL, MULTI-USER) */

// Allows multiple accounts to be created.
private fun userKey(username: String): String =
    "user_${username.trim().lowercase(Locale.getDefault())}"

// Creates a new account if the username is not already taken.
// Returns a Pair that includes whether the creation was successful.
private fun createAccount(
    context: Context,
    username: String,
    password: String
): Pair<Boolean, String> {
    if (username.isBlank() || password.isBlank()) {
        return false to "Please enter a username and password"
    }

    val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
    val key = userKey(username)

    // Do not allow two users with the same username.
    if (prefs.contains(key)) {
        return false to "That username is already in use. Please choose another one."
    }

    // Save the password for this username key.
    prefs.edit()
        .putString(key, password)
        .apply()

    return true to "Account created! You can log in now."
}

// Validates the login attempt by reading the stored password for the given
// username and comparing it to the password that was entered on the login screen.
private fun validateLogin(
    context: Context,
    username: String,
    password: String
): Boolean {
    val prefs = context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
    val key = userKey(username)
    val storedPassword = prefs.getString(key, null)

    return storedPassword != null && storedPassword == password
}

/* SCREENS */

// Login screen where the user can enter their credentials or navigate
// to the Create Account screen.
@Composable
private fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    BackgroundWithOverlay {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "How Will You Revolv?",
                        fontSize = 28.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Sign in to track your daily weight and progress.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Username input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Password input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Login button validates the credentials through the callback.
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter username and password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onLogin(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text("Login")
                    }

                    // Navigate to the Create Account screen.
                    OutlinedButton(
                        onClick = onCreateAccountClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Create Account")
                    }
                }
            }
        }
    }
}

// Screen that allows the user to create a new account with a username and password.
@Composable
private fun CreateAccountScreen(
    onCreate: (String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val context = LocalContext.current

    BackgroundWithOverlay {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Set up your Revolv 360 login to start tracking your journey.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Username input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Password input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Confirm password input
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Validate the inputs for accuracy.
                    Button(
                        onClick = {
                            when {
                                username.isBlank() || password.isBlank() || confirm.isBlank() ->
                                    Toast.makeText(
                                        context,
                                        "Please complete all fields",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                password != confirm ->
                                    Toast.makeText(
                                        context,
                                        "Passwords do not match",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                else -> onCreate(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text("Save Account")
                    }

                    // Return to the login screen without creating an account.
                    OutlinedButton(
                        onClick = onBackToLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Back to Login")
                    }
                }
            }
        }
    }
}

// Home screen that greets the user and provides a logout button.
@Composable
private fun HomeScreen(
    username: String,
    onLogout: () -> Unit
) {
    BackgroundWithOverlay {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hello, $username!",
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "You are logged in.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

/* SHARED BACKGROUND */

// App image.
@Composable
private fun BackgroundWithOverlay(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fitness),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        content()
    }
}

/* PREVIEW */

// Preview of the Login screen so I can quickly check layout changes in Android Studio.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewLogin() {
    AppTheme(darkTheme = false) {
        LoginScreen(onLogin = { _, _ -> }, onCreateAccountClick = {})
    }
}
