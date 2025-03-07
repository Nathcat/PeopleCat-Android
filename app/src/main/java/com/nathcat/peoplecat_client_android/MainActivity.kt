package com.nathcat.peoplecat_client_android

import StartupLoading
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nathcat.peoplecat_client_android.networking.ServiceHandler
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor


class MainActivity : ComponentActivity() {
    private val serviceHandler: ServiceHandler = ServiceHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceHandler.init(this) {
            serviceHandler.waitUntilAuth { authenticated, user ->
                if (authenticated) {
                    println("Service is already authenticated")
                    val i = Intent(this, ChatListActivity::class.java)
                    i.putExtra("chatId", 1)
                    startActivity(i)
                }
                else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }

        setContent {
            PeopleCatAndroidClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor
                ) {
                    StartupLoading(gradientStart = gradientStart, gradientEnd = gradientEnd)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        println("onStop")

        serviceHandler.close()
    }

    override fun onStop() {
        super.onStop()

        println("onStop")

        serviceHandler.close()
    }

    override fun onResume() {
        super.onResume()

        println("onResume")

        serviceHandler.init(this)
    }
}
