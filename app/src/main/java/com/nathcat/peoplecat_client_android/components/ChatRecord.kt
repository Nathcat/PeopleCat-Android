package com.nathcat.peoplecat_client_android.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.json.simple.JSONObject

@Composable
fun ChatRecord(
    c: JSONObject,
    iconSize: Dp = 50.dp,
    modifier: Modifier = Modifier,
    onClick: (c: JSONObject) -> Unit = { },
) {
    Row(
        modifier = modifier
            .clickable { onClick(c) }
    ) {
        /*ProfilePicture(
            url = "https://cdn.nathcat.net/pfps/" + (c.get("pfpPath") as String),
            size = pfpSize,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.CenterVertically)
        )*/

        Text(
            text = c.get("name") as String,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}