package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.icons.Icon
import com.adevinta.spark.components.text.Text
import com.adevinta.spark.icons.FavoriteFill
import com.adevinta.spark.icons.FavoriteOutline
import com.adevinta.spark.icons.SparkIcons
import fr.leboncoin.androidrecruitmenttestapp.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.AlbumGridCard
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.ErrorMessage
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.LoadingIndicator
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    onAlbumClick: (Int) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()

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
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshAlbums() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AlbumsTopBar(
                            showFavoritesOnly = showFavoritesOnly,
                            onToggleFavorites = { viewModel.toggleShowFavoritesOnly() }
                        )

                        AlbumsList(
                            albums = state.data,
                            onAlbumClick = onAlbumClick,
                            onToggleFavorite = { albumId ->
                                viewModel.toggleFavorite(albumId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumsTopBar(
    showFavoritesOnly: Boolean,
    onToggleFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showFavoritesOnly) "Favoris" else "Bibliothèque",
                style = SparkTheme.typography.display3.copy(
                    fontWeight = FontWeight.Bold,
                    color = SparkTheme.colors.onSurface
                )
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (showFavoritesOnly)
                            SparkTheme.colors.main
                        else
                            SparkTheme.colors.onSurface.copy(alpha = 0.1f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleFavorites
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    sparkIcon = if (showFavoritesOnly) SparkIcons.FavoriteFill else SparkIcons.FavoriteOutline,
                    contentDescription = null,
                    tint = if (showFavoritesOnly)
                        SparkTheme.colors.onMain
                    else
                        SparkTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (showFavoritesOnly) "Vos albums préférés" else "Tous vos albums",
            style = SparkTheme.typography.body2.copy(
                color = SparkTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
private fun AlbumsList(
    albums: List<Album>,
    onAlbumClick: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Aucun album",
                    style = SparkTheme.typography.headline2.copy(
                        color = SparkTheme.colors.onSurface
                    )
                )
                Text(
                    text = "Vos albums apparaîtront ici",
                    style = SparkTheme.typography.body2.copy(
                        color = SparkTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = albums,
                key = { it.id }
            ) { album ->
                AlbumGridCard(
                    album = album,
                    onClick = { onAlbumClick(album.id) },
                    onToggleFavorite = { onToggleFavorite(album.id) }
                )
            }
        }
    }
}
