package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.buttons.ButtonFilled
import com.adevinta.spark.components.scaffold.Scaffold
import fr.leboncoin.androidrecruitmenttestapp.AlbumsViewModel
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.ErrorMessage
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.LoadingIndicator
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album

@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    onItemSelected: (Album) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        when (val state = uiState) {
            is UiState.Initial -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }

            is UiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ErrorMessage(message = state.message)
                    Spacer(modifier = Modifier.height(16.dp))
                    ButtonFilled(onClick = { viewModel.refresh() }) {
                        Text("Réessayer")
                    }
                }
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun album disponible",
                            style = SparkTheme.typography.body1,
                            color = SparkTheme.colors.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = paddingValues,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = state.data,
                            key = { album -> album.id }
                        ) { album ->
                            AlbumItem(
                                album = album,
                                onItemSelected = onItemSelected
                            )
                        }
                    }
                }
            }
        }
    }
}