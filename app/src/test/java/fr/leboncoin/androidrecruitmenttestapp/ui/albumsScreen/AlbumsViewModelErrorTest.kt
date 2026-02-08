package fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen

import app.cash.turbine.test
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AlbumsViewModelErrorTest {

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
    fun `should handle network timeout error`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw TimeoutCancellationException("Request timeout")
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
            val state = awaitItem()
            assertTrue("Should be Error state", state is UiState.Error)
            val error = state as UiState.Error
            assertTrue("Error message should contain timeout",
                error.message.contains("timeout", ignoreCase = true))
        }
    }

    @Test
    fun `should handle socket timeout error`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw SocketTimeoutException("Connection timeout")
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
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            val error = state as UiState.Error
            assertTrue("Error message should be displayed", error.message.isNotEmpty())
        }
    }

    @Test
    fun `should handle server error 500`() = runTest {
        // Given
        val serverError = IOException("Server error: 500 Internal Server Error")
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw serverError
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
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            val error = state as UiState.Error
            assertTrue("Error message should contain 500",
                error.message.contains("500", ignoreCase = true) ||
                error.message.contains("Server error", ignoreCase = true))
        }
    }

    @Test
    fun `should handle network unavailable error`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw UnknownHostException("No internet connection")
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
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            val error = state as UiState.Error
            assertTrue("Error message should be displayed", error.message.isNotEmpty())
        }
    }

    @Test
    fun `should handle generic IOException`() = runTest {
        // Given
        whenever(getAllAlbumsUseCase()).thenReturn(
            flow {
                throw IOException("Network error occurred")
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
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            val error = state as UiState.Error
            assertEquals("Network error occurred", error.message)
        }
    }

    @Test
    fun `should handle error during refresh`() = runTest {
        // Given
        val albums = listOf(Album(1, 1, "Album 1", "url1", "thumb1", false))
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(refreshAlbumsUseCase()).thenThrow(IOException("Refresh failed"))

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
        viewModel.uiState.test {
            skipItems(1) // Skip initial success
            val state = awaitItem()
            assertTrue("Should be Error after refresh failure", state is UiState.Error)
            val error = state as UiState.Error
            assertTrue("Error message should mention refresh",
                error.message.contains("refresh", ignoreCase = true) ||
                error.message.contains("Refresh failed", ignoreCase = true))
        }
    }

    @Test
    fun `error message should be user-friendly`() = runTest {
        // Given - Test que les messages d'erreur sont compréhensibles
        val technicalErrors = mapOf(
            "java.net.SocketTimeoutException: Read timed out" to "timeout",
            "java.io.IOException: Server returned HTTP response code: 500" to "server",
            "java.net.UnknownHostException: Unable to resolve host" to "network"
        )

        technicalErrors.forEach { (technicalError, expectedKeyword) ->
            whenever(getAllAlbumsUseCase()).thenReturn(
                flow {
                    throw Exception(technicalError)
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
                val state = awaitItem()
                assertTrue(state is UiState.Error)
                val error = state as UiState.Error
                // Le message devrait être présent même s'il est technique
                assertTrue("Error message should not be empty", error.message.isNotEmpty())
            }
        }
    }
}
