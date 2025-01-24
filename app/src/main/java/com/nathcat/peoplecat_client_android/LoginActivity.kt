package com.nathcat.peoplecat_client_android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_client_android.ui.theme.secondaryColor


class LoginActivity: ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PeopleCatAndroidClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor
                ) {
                    var username by remember { mutableStateOf(TextFieldValue("")) }
                    var password by remember { mutableStateOf(TextFieldValue("")) }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .background(
                                        color = secondaryColor,
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Welcome to PeopleCat!",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Text(
                                        text = "Please login using your AuthCat account. Your login will be saved on your device so you won't need to do this again!"
                                    )
                                }
                            }

                            TextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(PaddingValues(vertical = 10.dp))
                            )

                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(PaddingValues(vertical = 10.dp))
                            )

                            Button(
                                onClick = {
                                    FileManager.writeUserData(this@LoginActivity, username.text, password.text)
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                },
                                modifier = Modifier
                                    .padding(PaddingValues(vertical = 10.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(
                                                gradientStart,
                                                gradientEnd
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Login", color = primaryColor)
                            }
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0);
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginPreview() {
    PeopleCatAndroidClientTheme {
        PeopleCatAndroidClientTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = primaryColor
            ) {
                var username by remember { mutableStateOf(TextFieldValue("")) }
                var password by remember { mutableStateOf(TextFieldValue("")) }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .background(
                                    color = secondaryColor,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Welcome to PeopleCat!",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Text(
                                    text = "Please login using your AuthCat account. Your login will be saved on your device so you won't need to do this again!"
                                )
                            }
                        }

                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(PaddingValues(vertical = 10.dp))
                        )

                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(PaddingValues(vertical = 10.dp))
                        )

                        Button(
                            onClick = {},
                            modifier = Modifier
                                .padding(PaddingValues(vertical = 10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            gradientStart,
                                            gradientEnd
                                        )
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Login", color = primaryColor)
                        }
                    }
                }
            }
        }
    }
}