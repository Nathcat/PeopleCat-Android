package com.nathcat.peoplecat_client_android.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nathcat.peoplecat_client_android.ui.theme.PeopleCatAndroidClientTheme
import com.nathcat.peoplecat_client_android.ui.theme.primaryColor
import com.nathcat.peoplecat_client_android.ui.theme.quadColor
import org.json.simple.JSONObject

@Composable
fun PageHeader(
    showButton: Boolean = false,
    buttonContent: @Composable RowScope.() -> Unit = { },
    buttonOnClick: () -> Unit = { },

    showUser: Boolean = false,
    user: JSONObject? = null,

    titleText: String = "PeopleCat"
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (showButton) {
                Button(
                    onClick = buttonOnClick,
                    content = buttonContent,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .padding(PaddingValues(horizontal = 5.dp))
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(100)
                        )
                        .align(Alignment.CenterVertically)
                        .border(
                            width = 2.dp,
                            color = quadColor,
                            shape = RoundedCornerShape(100)
                        )
                        .align(Alignment.CenterVertically)
                )
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterVertically)
            )

            if (showUser) {
                assert(user != null)

                UserRecord(
                    u = user!!,
                    pfpSize = 50.dp,
                    showUsername = false,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
        }

        HorizontalDivider(
            thickness = 2.dp,
            color = quadColor
        )
    }
}

@Preview
@Composable
fun PageHeader_Preview() {
    PeopleCatAndroidClientTheme {
        Surface(
            color = primaryColor,
            modifier = Modifier
                .fillMaxSize()
        ) {
            var u = JSONObject()
            u.put("username", "Nathcat")
            u.put("fullName", "Nathan Baines")
            u.put("pfpPath", "6751b1fe51a9f7.15602042.jpg")

            PageHeader(
                showButton = true,
                buttonContent = { Text("Back") },
                buttonOnClick = { },

                showUser = true,
                user = u,

                titleText = "PeopleCat"
            )
        }
    }
}