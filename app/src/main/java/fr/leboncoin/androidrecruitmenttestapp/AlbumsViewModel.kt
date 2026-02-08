package fr.leboncoin.androidrecruitmenttestapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            combine(
                getAllAlbumsUseCase(),
                getFavoriteAlbumsUseCase(),
                _showFavoritesOnly
            ) { allAlbums, favoriteAlbums, showFavorites ->
                if (showFavorites) favoriteAlbums else allAlbums
            }
                .catch { e ->
                    _uiState.value = UiState.Error(
                        e.message ?: "Error occurred"
                    )
                }
                .collect { albums ->
                    _uiState.value = UiState.Success(albums)
                }
        }

        // Load data from the API in the background
        viewModelScope.launch {
            try {
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                // The error will be handled by the above Flow.
            }
        }
    }

    fun refreshAlbums() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Error during refresh"
                )
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(albumId)
            } catch (e: Exception) {
                // Error handling - could show a snackbar
            }
        }
    }

    fun toggleShowFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }
}