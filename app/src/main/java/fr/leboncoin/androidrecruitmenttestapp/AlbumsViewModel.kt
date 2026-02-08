package fr.leboncoin.androidrecruitmenttestapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumsUiState(
    val albums: List<fr.leboncoin.domain.model.Album> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumsUiState(isLoading = true))
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            getAllAlbumsUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Une erreur est survenue"
                    )
                }
                .collect { albums ->
                    _uiState.value = _uiState.value.copy(
                        albums = albums,
                        isLoading = false
                    )
                }
        }

        // Charger les données depuis l'API
        viewModelScope.launch {
            try {
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                // L'erreur sera gérée par le Flow ci-dessus
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur lors du rafraîchissement"
                )
            }
        }
    }
}