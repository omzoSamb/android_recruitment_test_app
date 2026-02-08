package fr.leboncoin.androidrecruitmenttestapp.ui.detailScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.icons.Icon
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.icons.ArrowLeft
import com.adevinta.spark.icons.FavoriteFill
import com.adevinta.spark.icons.FavoriteOutline
import com.adevinta.spark.icons.SparkIcons
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.ErrorMessage
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.LoadingIndicator
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    albumId: Int,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(albumId) {
        viewModel.loadAlbum(albumId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SparkTheme.colors.surface)
    ) {
        when (val state = uiState) {
            is UiState.Initial, is UiState.Loading -> {
                LoadingIndicator()
            }

            is UiState.Error -> {
                ErrorMessage(message = state.message)
            }

            is UiState.Success -> {
                AlbumDetailContent(
                    album = state.data,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onBack = {
                        (context as? android.app.Activity)?.finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun AlbumDetailContent(
    album: Album,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.url)
                        .httpHeaders(
                            NetworkHeaders.Builder()
                                .add("User-Agent", "LeboncoinApp/1.0")
                                .build()
                        )
                        .crossfade(300)
                        .build(),
                    contentDescription = album.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SparkTheme.colors.surface.copy(alpha = 0.3f),
                                    SparkTheme.colors.surface.copy(alpha = 0.8f),
                                    SparkTheme.colors.surface
                                )
                            )
                        )
                )

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.url)
                        .httpHeaders(
                            NetworkHeaders.Builder()
                                .add("User-Agent", "LeboncoinApp/1.0")
                                .build()
                        )
                        .crossfade(300)
                        .build(),
                    contentDescription = album.title,
                    modifier = Modifier
                        .size(280.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SparkTheme.colors.surface.copy(alpha = 0.9f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        sparkIcon = SparkIcons.ArrowLeft,
                        contentDescription = "Retour",
                        tint = SparkTheme.colors.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = album.title,
                    style = SparkTheme.typography.display2.copy(
                        fontWeight = FontWeight.Bold,
                        color = SparkTheme.colors.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(text = "Album #${album.albumId}")
                    InfoChip(text = "Track #${album.id}")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (album.isFavorite)
                                SparkTheme.colors.main
                            else
                                SparkTheme.colors.onSurface.copy(alpha = 0.15f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleFavorite
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            sparkIcon = if (album.isFavorite) SparkIcons.FavoriteFill else SparkIcons.FavoriteOutline,
                            contentDescription = null,
                            tint = if (album.isFavorite)
                                SparkTheme.colors.onMain
                            else
                                SparkTheme.colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (album.isFavorite) "Dans vos favoris" else "Ajouter aux favoris",
                            style = SparkTheme.typography.body1.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (album.isFavorite)
                                    SparkTheme.colors.onMain
                                else
                                    SparkTheme.colors.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SparkTheme.colors.onSurface.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = SparkTheme.typography.caption.copy(
                color = SparkTheme.colors.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        )
    }
}
