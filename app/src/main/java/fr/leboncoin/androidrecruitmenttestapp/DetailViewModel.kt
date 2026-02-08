package fr.leboncoin.androidrecruitmenttestapp

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAlbumByIdUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAlbumByIdUseCase: GetAlbumByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Album>>(UiState.Initial)
    val uiState: StateFlow<UiState<Album>> = _uiState.asStateFlow()

    fun loadAlbum(albumId: Int) {
        savedStateHandle["albumId"] = albumId // Save ID into savedStateHandle
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val album = getAlbumByIdUseCase(albumId)
                if (album != null) {
                    _uiState.value = UiState.Success(album)
                } else {
                    _uiState.value = UiState.Error("Album not found")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Error while loading"
                )
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is UiState.Success) {
                try {
                    val newFavoriteStatus = toggleFavoriteUseCase(currentState.data.id)
                    _uiState.value = UiState.Success(
                        currentState.data.copy(isFavorite = newFavoriteStatus)
                    )
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(
                        e.message ?: "Error during update"
                    )
                }
            }
        }
    }
}