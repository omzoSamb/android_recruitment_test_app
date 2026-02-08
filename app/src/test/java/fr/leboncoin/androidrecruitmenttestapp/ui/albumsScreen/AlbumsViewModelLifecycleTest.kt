package fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AlbumsViewModelLifecycleTest {

    private lateinit var getAllAlbumsUseCase: GetAllAlbumsUseCase
    private lateinit var getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase
    private lateinit var refreshAlbumsUseCase: RefreshAlbumsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var viewModel: AlbumsViewModel

    @Before
    fun setup() {
        getAllAlbumsUseCase = mock()
        getFavoriteAlbumsUseCase = mock()
        refreshAlbumsUseCase = mock()
        toggleFavoriteUseCase = mock()
    }

    @Test
    fun `complete lifecycle: Initial -> Loading -> Success with data`() = runTest {
        // Given - Simuler un chargement progressif
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                emit(emptyList()) // État initial
                delay(100) // Simuler un délai de chargement
                emit(listOf(
                    Album(1, 1, "Album 1", "url1", "thumb1", false),
                    Album(2, 1, "Album 2", "url2", "thumb2", false)
                ))
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = AlbumsViewModel(
            context = mock(),
            getAllAlbumsUseCase = getAllAlbumsUseCase,
            getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
            refreshAlbumsUseCase = refreshAlbumsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        // When & Then
        viewModel.uiState.test {
            // 1. État initial (peut être Initial ou Success avec liste vide)
            val initialState = awaitItem()
            assertTrue("Step 1: Initial state",
                initialState is UiState.Initial ||
                initialState is UiState.Success && (initialState as UiState.Success).data.isEmpty())

            // 2. État de chargement (si visible)
            // Note: Avec combine, on peut passer directement à Success

            // 3. État de succès avec données
            val successState = awaitItem()
            assertTrue("Step 3: Success state", successState is UiState.Success)
            val data = (successState as UiState.Success).data
            assertEquals("Should have 2 albums", 2, data.size)
            assertEquals("First album ID should be 1", 1, data[0].id)
            assertEquals("Second album ID should be 2", 2, data[1].id)
        }
    }

    @Test
    fun `lifecycle with error: Initial -> Loading -> Error`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                delay(50) // Simuler un délai
                throw Exception("Network timeout")
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = AlbumsViewModel(
            context = mock(),
            getAllAlbumsUseCase = getAllAlbumsUseCase,
            getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
            refreshAlbumsUseCase = refreshAlbumsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        // When & Then
        viewModel.uiState.test {
            // 1. État initial
            val initialState = awaitItem()
            assertTrue("Step 1: Initial state",
                initialState is UiState.Initial || initialState is UiState.Success)

            // 2. État d'erreur
            val errorState = awaitItem()
            assertTrue("Step 2: Error state", errorState is UiState.Error)
            val error = errorState as UiState.Error
            assertTrue("Error message should contain timeout",
                error.message.contains("timeout", ignoreCase = true))
        }
    }

    @Test
    fun `lifecycle with refresh: Success -> Refreshing -> Success`() = runTest {
        // Given
        val initialAlbums = listOf(Album(1, 1, "Album 1", "url1", "thumb1", false))
        val refreshedAlbums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", false)
        )

        var callCount = 0
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                callCount++
                if (callCount == 1) {
                    emit(initialAlbums)
                } else {
                    emit(refreshedAlbums)
                }
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = AlbumsViewModel(
            context = mock(),
            getAllAlbumsUseCase = getAllAlbumsUseCase,
            getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
            refreshAlbumsUseCase = refreshAlbumsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        // When
        viewModel.refreshAlbums()

        // Then
        viewModel.isRefreshing.test {
            // 1. État de rafraîchissement
            val refreshing = awaitItem()
            assertTrue("Step 1: Should be refreshing", refreshing)

            // 2. Fin du rafraîchissement
            val notRefreshing = awaitItem()
            assertTrue("Step 2: Should not be refreshing", !notRefreshing)
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val data = (state as UiState.Success).data
            assertEquals("Should have 2 albums after refresh", 2, data.size)
        }
    }
}
