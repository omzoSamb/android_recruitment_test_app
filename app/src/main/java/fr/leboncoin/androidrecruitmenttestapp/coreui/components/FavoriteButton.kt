package fr.leboncoin.androidrecruitmenttestapp.coreui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.icons.Icon
import com.adevinta.spark.icons.FavoriteFill
import com.adevinta.spark.icons.FavoriteOutline
import com.adevinta.spark.icons.SparkIcons

import com.adevinta.spark.tokens.contentColorFor

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            sparkIcon = if (isFavorite) SparkIcons.FavoriteFill else SparkIcons.FavoriteOutline,
            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
            modifier = Modifier.size(24.dp),
            tint = contentColorFor(
                backgroundColor = SparkTheme.colors.surface
            )
        )
    }
}