package com.nathcat.peoplecat_client_android.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nathcat.peoplecat_client_android.R
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart

@Composable
fun ProfilePicture(url: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(gradientStart, gradientEnd)), shape = CircleShape)
            .clip(CircleShape)
            .size(size)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "User's profile picture",
            modifier = Modifier
                .clip(CircleShape)
                .size(size.times(0.95f))
        )
    }
}