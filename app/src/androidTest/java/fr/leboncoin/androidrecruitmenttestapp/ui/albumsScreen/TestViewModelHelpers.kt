package fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen

import android.content.Context
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Helper function pour créer un ViewModel de test avec des StateFlows personnalisés
 * Pour les tests Compose, on mock les Use Cases pour retourner les flows désirés
 */
fun createTestViewModel(
    uiState: MutableStateFlow<UiState<List<Album>>> = MutableStateFlow(UiState.Initial),
    isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false),
    showFavoritesOnly: MutableStateFlow<Boolean> = MutableStateFlow(false)
): AlbumsViewModel {
    val getAllAlbumsUseCase: GetAllAlbumsUseCase = mock()
    val getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase = mock()
    val refreshAlbumsUseCase: RefreshAlbumsUseCase = mock()
    val toggleFavoriteUseCase: ToggleFavoriteUseCase = mock()
    val context: Context = mock()

    // Configurer les Use Cases pour retourner les données selon l'état
    val albums = when (val state = uiState.value) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    return AlbumsViewModel(
        context = context,
        getAllAlbumsUseCase = getAllAlbumsUseCase,
        getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
        refreshAlbumsUseCase = refreshAlbumsUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase
    )
}

/**
 * Helper function pour créer un ViewModel de test avec gestion des favoris
 */
fun createTestViewModelWithFavorites(
    allAlbums: List<Album>,
    favorites: List<Album>
): AlbumsViewModel {
    val getAllAlbumsUseCase: GetAllAlbumsUseCase = mock()
    val getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase = mock()
    val refreshAlbumsUseCase: RefreshAlbumsUseCase = mock()
    val toggleFavoriteUseCase: ToggleFavoriteUseCase = mock()
    val context: Context = mock()

    whenever(getAllAlbumsUseCase()).thenReturn(flowOf(allAlbums))
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(favorites))

    return AlbumsViewModel(
        context = context,
        getAllAlbumsUseCase = getAllAlbumsUseCase,
        getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
        refreshAlbumsUseCase = refreshAlbumsUseCase,
        toggleFavoriteUseCase = toggleFavoriteUseCase
    )
}
