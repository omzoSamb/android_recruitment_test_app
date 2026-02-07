package fr.leboncoin.androidrecruitmenttestapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class AlbumsViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {

    val albums: StateFlow<List<AlbumDto>> = repository.albums
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Reste actif 5s après que l'UI n'écoute plus
            initialValue = emptyList() // Valeur initiale en attendant la première émission
        )

    init {
        // Charger les données dès que le ViewModel est créé
        loadAlbums()
    }

    /*fun loadAlbums() {
        GlobalScope.launch {
            try {
                _albums.emit(repository.getAllAlbums())
            } catch (_: Exception) { /* TODO: Handle errors */ }
        }
    }*/



    private fun loadAlbums() {
        // On utilise viewModelScope, qui est lié au cycle de vie du ViewModel [11]
        viewModelScope.launch {
            try {
                repository.getAllAlbums()
            } catch (_: Exception) { /* TODO: Gérer les erreurs, ex: via un autre Flow pour l'UI */ }
        }
    }

    class Factory(
        private val repository: AlbumRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlbumsViewModel(repository) as T
        }
    }
}