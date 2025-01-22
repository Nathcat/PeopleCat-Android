import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
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
import com.nathcat.peoplecat_client_android.R
import com.nathcat.peoplecat_client_android.ui.theme.gradientEnd
import com.nathcat.peoplecat_client_android.ui.theme.gradientStart

@Composable
fun StartupLoading(gradientStart: Color, gradientEnd: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    val brush = Brush.verticalGradient(listOf(gradientStart, gradientEnd))
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush, blendMode = BlendMode.SrcAtop)
                    }
                }
        )
        Image(
            painter = painterResource(id = R.drawable.cat),
            contentDescription = "Cat image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(75.dp)
                .align(Alignment.Center)
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StartupLoading(gradientStart, gradientEnd)
}