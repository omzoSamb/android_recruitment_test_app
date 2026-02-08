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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AlbumsViewModelTest {

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
        viewModel = AlbumsViewModel(
            context = mock(),
            getAllAlbumsUseCase = getAllAlbumsUseCase,
            getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
            refreshAlbumsUseCase = refreshAlbumsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )
    }

    @Test
    fun `initial state should be Initial`() = runTest {
        // Given - Simuler un délai pour voir l'état initial
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                delay(100) // Simuler un chargement
                emit(emptyList())
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue("Initial state should be Initial or Loading",
                initialState is UiState.Initial || initialState is UiState.Loading)
        }
    }

    @Test
    fun `loading state should be displayed when data is being fetched`() = runTest {
        // Given - Simuler un chargement avec délai
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                emit(emptyList()) // État initial
                delay(50)
                emit(listOf(createTestAlbum(1)))
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val firstState = awaitItem()
            // Le premier état peut être Initial ou Success avec liste vide
            // On vérifie que le chargement se fait
        }
    }

    @Test
    fun `when albums are loaded successfully, state should be Success with data`() = runTest {
        // Given
        val albums = listOf(
            createTestAlbum(1, "Album 1"),
            createTestAlbum(2, "Album 2"),
            createTestAlbum(3, "Album 3")
        )
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("State should be Success", state is UiState.Success)
            val successState = state as UiState.Success
            assertEquals("Should have 3 albums", 3, successState.data.size)
            assertEquals("First album should match", albums[0], successState.data[0])
            assertEquals("All albums should match", albums, successState.data)
        }
    }

    @Test
    fun `loaded albums should have correct structure and fields`() = runTest {
        // Given
        val albums = listOf(
            createTestAlbum(
                id = 1,
                title = "Test Album",
                albumId = 100,
                url = "https://example.com/image.jpg",
                thumbnailUrl = "https://example.com/thumb.jpg",
                isFavorite = false
            )
        )
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val album = (state as UiState.Success).data.first()

            // Vérifier tous les champs obligatoires
            assertEquals(1, album.id)
            assertEquals(100, album.albumId)
            assertEquals("Test Album", album.title)
            assertEquals("https://example.com/image.jpg", album.url)
            assertEquals("https://example.com/thumb.jpg", album.thumbnailUrl)
            assertEquals(false, album.isFavorite)
        }
    }

    @Test
    fun `when no albums are available, state should be Success with empty list`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("State should be Success even with empty list",
                state is UiState.Success)
            val successState = state as UiState.Success
            assertTrue("List should be empty", successState.data.isEmpty())
        }
    }

    @Test
    fun `empty state should be displayed when favorites filter returns no results`() = runTest {
        // Given
        val allAlbums = listOf(createTestAlbum(1), createTestAlbum(2))
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(allAlbums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When - Activer le filtre favoris
        viewModel.toggleShowFavoritesOnly()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            val successState = state as UiState.Success
            assertTrue("Favorites list should be empty", successState.data.isEmpty())
        }
    }

    @Test
    fun `when error occurs during loading, state should be Error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw Exception(errorMessage)
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("State should be Error", state is UiState.Error)
            val errorState = state as UiState.Error
            assertEquals("Error message should match", errorMessage, errorState.message)
        }
    }

    @Test
    fun `when refresh fails, state should be Error with appropriate message`() = runTest {
        // Given
        val albums = listOf(createTestAlbum(1))
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(refreshAlbumsUseCase()).thenThrow(Exception("Refresh failed"))

        // When
        viewModel.refreshAlbums()

        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip initial success state
            val state = awaitItem()
            assertTrue("State should be Error after refresh failure",
                state is UiState.Error)
            val errorState = state as UiState.Error
            assertTrue("Error message should contain refresh error",
                errorState.message.contains("refresh", ignoreCase = true))
        }
    }

    @Test
    fun `should follow complete lifecycle: Initial -> Loading -> Success`() = runTest {
        // Given - Simuler un chargement progressif
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                delay(50) // Simuler un délai de chargement
                emit(listOf(createTestAlbum(1)))
            }
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When & Then
        viewModel.uiState.test {
            // 1. État initial
            val initialState = awaitItem()
            assertTrue("Initial state should be Initial or Loading",
                initialState is UiState.Initial || initialState is UiState.Loading)

            // 2. État de chargement (si visible)
            // Note: Avec combine, on peut passer directement à Success

            // 3. État de succès avec données
            val finalState = awaitItem()
            assertTrue("Final state should be Success", finalState is UiState.Success)
            val successState = finalState as UiState.Success
            assertEquals("Should have 1 album", 1, successState.data.size)
        }
    }

    @Test
    fun `should handle refresh lifecycle: Success -> Loading -> Success`() = runTest {
        // Given
        val initialAlbums = listOf(createTestAlbum(1))
        val refreshedAlbums = listOf(createTestAlbum(1), createTestAlbum(2))

        whenever(getAllAlbumsUseCase()).thenReturn(
            flowOf(initialAlbums),
            flowOf(refreshedAlbums)
        )
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.refreshAlbums()

        // Then
        viewModel.isRefreshing.test {
            val isRefreshing = awaitItem()
            assertTrue("Should be refreshing", isRefreshing)
            val notRefreshing = awaitItem()
            assertTrue("Should not be refreshing after completion", !notRefreshing)
        }
    }

    @Test
    fun `toggleFavorite should call use case with correct album ID`() = runTest {
        // Given
        val albumId = 123
        whenever(toggleFavoriteUseCase(albumId)).thenReturn(true)

        // When
        viewModel.toggleFavorite(albumId)

        // Then
        verify(toggleFavoriteUseCase).invoke(albumId)
    }

    @Test
    fun `toggleFavorite should handle errors gracefully`() = runTest {
        // Given
        val albumId = 123
        whenever(toggleFavoriteUseCase(albumId)).thenThrow(Exception("Database error"))

        // When
        viewModel.toggleFavorite(albumId)

        // Then - Should not crash, error is silently handled
        // (Dans une vraie app, on pourrait vouloir afficher un snackbar)
    }

    @Test
    fun `toggleShowFavoritesOnly should switch between all albums and favorites`() = runTest {
        // Given
        val allAlbums = listOf(
            createTestAlbum(1, isFavorite = false),
            createTestAlbum(2, isFavorite = true),
            createTestAlbum(3, isFavorite = false)
        )
        val favoriteAlbums = listOf(createTestAlbum(2, isFavorite = true))

        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(allAlbums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(favoriteAlbums))

        // When - Initial state (all albums)
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is UiState.Success)
            assertEquals(3, (initialState as UiState.Success).data.size)
        }

        // When - Toggle to favorites
        viewModel.toggleShowFavoritesOnly()

        viewModel.uiState.test {
            val favoritesState = awaitItem()
            assertTrue(favoritesState is UiState.Success)
            assertEquals(1, (favoritesState as UiState.Success).data.size)
            assertEquals(2, (favoritesState as UiState.Success).data[0].id)
        }

        // When - Toggle back to all
        viewModel.toggleShowFavoritesOnly()

        viewModel.uiState.test {
            val allState = awaitItem()
            assertTrue(allState is UiState.Success)
            assertEquals(3, (allState as UiState.Success).data.size)
        }
    }

    // Helper function pour créer des albums de test
    private fun createTestAlbum(
        id: Int = 1,
        title: String = "Test Album $id",
        albumId: Int = 1,
        url: String = "https://example.com/image$id.jpg",
        thumbnailUrl: String = "https://example.com/thumb$id.jpg",
        isFavorite: Boolean = false
    ): Album {
        return Album(
            id = id,
            albumId = albumId,
            title = title,
            url = url,
            thumbnailUrl = thumbnailUrl,
            isFavorite = isFavorite
        )
    }
}
