package fr.leboncoin.androidrecruitmenttestapp.coreui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.tokens.darkSparkColors
import com.adevinta.spark.tokens.lightSparkColors

val MusicColorsLight = lightSparkColors().copy(
    main = androidx.compose.ui.graphics.Color(0xFF6C5CE7),
    onMain = androidx.compose.ui.graphics.Color.White,
    support = androidx.compose.ui.graphics.Color(0xFFA29BFE),
    onSupport = androidx.compose.ui.graphics.Color.White,
    surface = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
    onSurface = androidx.compose.ui.graphics.Color(0xFF2D3436),
    backgroundVariant = androidx.compose.ui.graphics.Color(0xFFE9ECEF),
    error = androidx.compose.ui.graphics.Color(0xFFE74C3C)
)

val MusicColorsDark = darkSparkColors().copy(
    main = androidx.compose.ui.graphics.Color(0xFF8B7ED8),
    onMain = androidx.compose.ui.graphics.Color(0xFF2D3436),
    support = androidx.compose.ui.graphics.Color(0xFFB8B3FF),
    onSupport = androidx.compose.ui.graphics.Color(0xFF2D3436),
    surface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE9ECEF),
    backgroundVariant = androidx.compose.ui.graphics.Color(0xFF2D3436),
    error = androidx.compose.ui.graphics.Color(0xFFE74C3C)
)

@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) MusicColorsDark else MusicColorsLight

    SparkTheme(colors = colors) {
        content()
    }
}
