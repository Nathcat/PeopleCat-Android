package com.nathcat.peoplecat_client_android

import StartupLoading
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nathcat.peoplecat_client_android.components.ProfilePicture
import com.nathcat.peoplecat_client_android.networking.NetworkerService
import com.nathcat.peoplecat_client_android.networking.ServiceHandler
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_server.Packet
import org.json.simple.JSONObject


class UserActivity: ComponentActivity() {
    private val serviceHandler: ServiceHandler = ServiceHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PeopleCatAndroidClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor
                ) {
                    var user by remember { mutableStateOf(JSONObject()) }

                    if (user.contains("id")) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(50.dp)
                        ) {
                            ProfilePicture(url = "https://cdn.nathcat.net/pfps/" + user.get("pfpPath"), 200.dp)

                            Text(user.get("fullName") as String)

                            Text(
                                text = user.get("username") as String,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    else {
                        if (!serviceHandler.isBound) {
                            serviceHandler.init(this) {
                                serviceHandler.waitUntilAuth { authenticated, u ->
                                    user = u
                                }
                            }
                        }
                        else {
                            serviceHandler.waitUntilAuth { authenticated, u ->
                                user = u
                            }
                        }

                        StartupLoading(gradientStart = gradientStart, gradientEnd = gradientEnd)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceHandler.close()
    }
}