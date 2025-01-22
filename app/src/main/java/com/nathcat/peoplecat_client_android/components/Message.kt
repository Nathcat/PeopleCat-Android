package com.nathcat.peoplecat_client_android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_client_android.ui.theme.secondaryColor
import com.nathcat.peoplecat_client_android.ui.theme.tertiaryColor
import org.json.simple.JSONObject

@Composable
fun Message(sender: JSONObject?, content: String, isFromMe: Boolean = false) {
    Box(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
    ) {
        var s: JSONObject
        if (sender == null) {
            s = JSONObject()
            s["username"] = "unknown"
            s["fullName"] = "unknown"
            s["pfpPath"] = "default.png"
        }
        else {
            s = sender
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isFromMe) {
                ProfilePicture(
                    url = "https://cdn.nathcat.net/pfps/" + s["pfpPath"] as String,
                    size = 50.dp,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterVertically)
                )

                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = s["fullName"] as String,
                        modifier = Modifier.padding(PaddingValues(horizontal = 12.5.dp))
                    )

                    Box(
                        modifier = Modifier
                            .background(secondaryColor, shape = RoundedCornerShape(25.dp))
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.75f)
                    ) {
                        Text(
                            text = content
                        )
                    }
                }
            }
            else {
                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        Spacer(Modifier.weight(1f))

                        Text(
                            text = s["fullName"] as String,
                            modifier = Modifier.padding(PaddingValues(horizontal = 12.5.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(tertiaryColor, shape = RoundedCornerShape(25.dp))
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(0.75f)
                    ) {
                        Text(
                            text = content,
                            color = primaryColor
                        )
                    }
                }

                ProfilePicture(
                    url = "https://cdn.nathcat.net/pfps/" + s["pfpPath"] as String,
                    size = 50.dp,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }

    }
}