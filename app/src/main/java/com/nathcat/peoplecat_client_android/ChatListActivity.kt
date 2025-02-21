package com.nathcat.peoplecat_client_android

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nathcat.peoplecat_client_android.components.ChatRecord
import com.nathcat.peoplecat_client_android.components.PageHeader
import com.nathcat.peoplecat_client_android.networking.ServiceHandler
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_client_android.ui.theme.quadColor
import com.nathcat.peoplecat_server.Packet
import org.json.simple.JSONObject
import java.util.Random


class ChatListActivity : ComponentActivity() {
    private val serviceHandler: ServiceHandler = ServiceHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PeopleCatAndroidClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor
                ) {
                    var chatList = remember { mutableStateListOf<JSONObject>() }
                    var user by remember { mutableStateOf(JSONObject()) }

                    if (!serviceHandler.isBound) {
                        serviceHandler.init(this) {
                            serviceHandler.waitUntilAuth { authenticated, u ->
                                if (!authenticated) {
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    return@waitUntilAuth
                                }

                                user = u

                                serviceHandler.register(Packet.TYPE_GET_CHAT_MEMBERSHIPS) { packets ->
                                    packets.forEach {p: Packet ->
                                        chatList.add(p.data)
                                    }

                                    return@register null;
                                }

                                serviceHandler.register(Packet.TYPE_NOTIFICATION_MESSAGE) { packets: Array<Packet> ->
                                    val userSearch =
                                        JSONObject()
                                    userSearch["id"] =
                                        (packets[0].data["message"] as JSONObject)["senderId"]
                                    val chatId = Math.toIntExact(
                                        (packets[0].data["chat"] as JSONObject)["chatId"] as Long
                                    )
                                    println(
                                        "Notifying: " + (packets[0].data["chat"] as JSONObject).toJSONString()
                                    )
                                    serviceHandler.register(Packet.TYPE_GET_USER) { search: Array<Packet> ->
                                            val intent = Intent(
                                                this@ChatListActivity,
                                                ChatActivity::class.java
                                            )
                                            intent.putExtra(
                                                "chat",
                                                (packets[0].data["chat"] as JSONObject).toJSONString()
                                            )
                                            intent.action = "chat"
                                            val pendingIntent =
                                                PendingIntent.getActivities(
                                                    this@ChatListActivity,
                                                    0,
                                                    arrayOf<Intent>(intent),
                                                    PendingIntent.FLAG_IMMUTABLE
                                                )
                                            val n =
                                                NotificationCompat.Builder(
                                                    this@ChatListActivity,
                                                    (this@ChatListActivity.getText(R.string.event_notif_channel) as String)!!
                                                )
                                                    .setSmallIcon(R.drawable.cat_notification)
                                                    .setContentTitle("New message")
                                                    .setContentText(
                                                        search[0].data["username"]
                                                            .toString() + " sent a message"
                                                    )
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true)
                                                    .build()
                                            if (ActivityCompat.checkSelfPermission(
                                                    this@ChatListActivity,
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                ) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                                System.err.println("Notification permission not granted!")
                                                return@register null
                                            }
                                            NotificationManagerCompat.from(this@ChatListActivity)
                                                .notify(Random().nextInt(), n)
                                            null
                                        }
                                    serviceHandler.send(
                                        arrayOf(Packet.createPacket(
                                            Packet.TYPE_GET_USER,
                                            true,
                                            userSearch
                                        ))
                                    )
                                    null
                                }

                                serviceHandler.send(arrayOf(Packet.createPacket(
                                    Packet.TYPE_GET_CHAT_MEMBERSHIPS,
                                    true,
                                    null
                                )))
                            }
                        }
                    }

                    Column {
                        PageHeader(
                            showUser = user.contains("username"),
                            user = user,
                        )

                        LazyColumn {
                            itemsIndexed(
                                items = chatList
                            ) {index, chat: JSONObject ->
                                ChatRecord(
                                    c = chat,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 100.dp)
                                        .padding(10.dp)
                                ) {
                                    var i = Intent(this@ChatListActivity, ChatActivity::class.java)
                                    i.putExtra("chat", chat.toJSONString())

                                    startActivity(i)
                                }

                                HorizontalDivider(color = quadColor)
                            }
                        }
                    }
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