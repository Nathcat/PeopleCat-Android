package com.nathcat.peoplecat_client_android.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.json.simple.JSONObject

@Composable
fun UserRecord(
    u: JSONObject,
    pfpSize: Dp,
    modifier: Modifier = Modifier,
    onClick: (u: JSONObject) -> Unit = { },
    showUsername: Boolean = true,
    showFullName: Boolean = true
) {
    Row(
        modifier = modifier
            .clickable { onClick(u) }
    ) {
        ProfilePicture(
            url = "https://cdn.nathcat.net/pfps/" + (u.get("pfpPath") as String),
            size = pfpSize,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.CenterVertically)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            if (showUsername) {
                Text(
                    text = u.get("fullName") as String,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            if (showFullName) {
                Text(
                    text = u.get("username") as String,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}