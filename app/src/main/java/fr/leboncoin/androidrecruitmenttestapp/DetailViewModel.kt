package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val album: Album? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState(isLoading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val album = getAlbumByIdUseCase(albumId)
                _uiState.value = _uiState.value.copy(
                    album = album,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du chargement"
                )
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _uiState.value.album?.let { album ->
                try {
                    val newFavoriteStatus = toggleFavoriteUseCase(album.id)
                    _uiState.value = _uiState.value.copy(
                        album = album.copy(isFavorite = newFavoriteStatus)
                    )
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}