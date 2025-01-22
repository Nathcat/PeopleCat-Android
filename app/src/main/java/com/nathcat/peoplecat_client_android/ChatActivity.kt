package com.nathcat.peoplecat_client_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.nathcat.peoplecat_client_android.components.Message
import com.nathcat.peoplecat_client_android.networking.ServiceHandler
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_client_android.ui.theme.quadColor
import com.nathcat.peoplecat_server.Packet
import org.json.simple.JSONObject
import java.util.Date


class ChatActivity: ComponentActivity() {
    private val serviceHandler: ServiceHandler = ServiceHandler()
    private var chatId: Int? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatId = intent.extras?.getInt("chatId")

        setContent {
            PeopleCatAndroidClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = primaryColor
                ) {
                    var user by remember { mutableStateOf(JSONObject()) }
                    var messages = remember { mutableStateListOf<JSONObject>() }
                    var userMap = remember { mutableStateMapOf<Long, JSONObject>() }
                    var listState = rememberLazyListState()
                    var messageEntry by remember { mutableStateOf(TextFieldValue("")) }

                    println("Have " + messages.size + " messages.")

                    LaunchedEffect(messages.size) {
                        listState.scrollToItem(messages.size)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            userScrollEnabled = true,
                            modifier = Modifier.fillMaxHeight(0.9f).fillMaxWidth()
                        ) {
                            itemsIndexed(
                                items = messages
                            ) { index, m ->
                                val sender = userMap.get(m.get("senderId"))

                                Message(sender, m.get("content") as String, if (sender != null) { sender["id"] == user["id"] } else { false })

                                if (sender == null) {
                                    serviceHandler.register(Packet.TYPE_GET_USER) { packets: Array<Packet> ->
                                        userMap[packets[0].data["id"] as Long] = packets[0].data

                                        return@register null
                                    }

                                    serviceHandler.register(Packet.TYPE_ERROR) { packets: Array<Packet> ->
                                        System.err.println("Error from server: " + packets[0].data["name"] + ": " + packets[0].data["msg"])

                                        return@register null
                                    }

                                    val searchData = JSONObject()
                                    searchData["id"] = m["senderId"]

                                    serviceHandler.send(
                                        arrayOf(
                                            Packet.createPacket(
                                                Packet.TYPE_GET_USER,
                                                true,
                                                searchData
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(PaddingValues(horizontal = 10.dp))
                                .align(Alignment.CenterHorizontally)
                        ) {
                            TextField(
                                value = messageEntry,
                                onValueChange = { messageEntry = it },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .fillMaxWidth(0.75f),
                                placeholder = { Text("Message...") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, capitalization = KeyboardCapitalization.Sentences),
                                keyboardActions = KeyboardActions(onSend = {
                                    sendMessage(messageEntry.text)
                                    messageEntry = TextFieldValue("")
                                })
                            )

                            IconButton(
                                onClick = {
                                    sendMessage(messageEntry.text)
                                    messageEntry = TextFieldValue("")
                                },
                                modifier = Modifier
                                    .padding(PaddingValues(horizontal = 5.dp))
                                    .background(
                                        brush = Brush.linearGradient(listOf(gradientStart, gradientEnd)),
                                        shape = CircleShape
                                    )
                                    .align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Send",
                                    tint = quadColor
                                )
                            }
                        }
                    }


                    if (!user.contains("id")) {
                        if (!serviceHandler.isBound) {
                            serviceHandler.init(this) {
                                serviceHandler.getState {authenticated, u ->
                                    if (authenticated) {
                                        user = u
                                        userMap[user["id"] as Long] = user
                                    }
                                    else {
                                        startActivity(Intent(this@ChatActivity, MainActivity::class.java))
                                    }
                                }
                            }
                        }
                        else {
                            serviceHandler.getState {authenticated, u ->
                                if (authenticated) {
                                    user = u
                                    userMap[user["id"] as Long] = user
                                }
                                else {
                                    startActivity(Intent(this@ChatActivity, MainActivity::class.java))
                                }
                            }
                        }
                    }
                    else {
                        if (messages.size == 0) {
                            serviceHandler.register(Packet.TYPE_GET_MESSAGE_QUEUE) { packets: Array<Packet> ->
                                println("Application got response from server!")
                                for (i in 1 until packets.size) {
                                    messages.add(packets[i].data)
                                }

                                serviceHandler.register(Packet.TYPE_NOTIFICATION_MESSAGE) {packets: Array<Packet> ->
                                    println("Application received message notification")
                                    if (Math.toIntExact(packets[0].data["chatId"] as Long) == chatId) {
                                        println("Chat ID and chat being viewed match!")
                                        messages.add(packets[0].data["message"] as JSONObject)
                                    }
                                    else {
                                        // TODO Send a notification here
                                    }

                                    return@register null
                                }

                                return@register null
                            }

                            val d = JSONObject();
                            d.put("chatId", chatId)
                            serviceHandler.send(
                                listOf(
                                    Packet.createPacket(
                                        Packet.TYPE_GET_MESSAGE_QUEUE,
                                        true,
                                        d
                                    )
                                ).toTypedArray()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceHandler.close()
    }

    override fun onStop() {
        super.onStop()

        serviceHandler.close()
    }

    fun sendMessage(content: String) {
        val messageData = JSONObject()
        messageData.put("chatId", chatId)
        messageData.put("content", content)
        messageData.put("timeSent", Date().time)

        serviceHandler.send(arrayOf(Packet.createPacket(
            Packet.TYPE_SEND_MESSAGE,
            true,
            messageData
        )))
    }
}