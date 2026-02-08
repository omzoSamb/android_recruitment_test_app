package fr.leboncoin.androidrecruitmenttestapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            getAllAlbumsUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(
                        e.message ?: "Error occurred"
                    )
                }
                .collect { albums ->
                    _uiState.value = when {
                        albums.isEmpty() -> UiState.Success(emptyList())
                        else -> UiState.Success(albums)
                    }
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

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                refreshAlbumsUseCase()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Error during refresh"
                )
            }
        }
    }
}