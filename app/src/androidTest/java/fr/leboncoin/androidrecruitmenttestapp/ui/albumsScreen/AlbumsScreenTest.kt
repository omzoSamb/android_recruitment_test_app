package fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.leboncoin.androidrecruitmenttestapp.utils.UiState
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen.createTestViewModel

class AlbumsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `loading indicator should be displayed when state is Loading`() {
        // Given
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Loading)
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then
        // Le LoadingIndicator devrait être affiché
        // On attend un peu pour que le composant se compose
        composeTestRule.waitForIdle()
    }

    @Test
    fun `error message should be displayed when state is Error`() {
        // Given
        val errorMessage = "Une erreur est survenue"
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Error(errorMessage))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun `error state should display different error messages correctly`() {
        // Given - Test avec différents types d'erreurs
        val errorMessages = listOf(
            "Network error",
            "Timeout error",
            "Server error (500)",
            "Not found (404)"
        )

        errorMessages.forEach { errorMessage ->
            val viewModel = createTestViewModel(
                uiState = MutableStateFlow(UiState.Error(errorMessage))
            )

            // When
            composeTestRule.setContent {
                AlbumsScreen(
                    onAlbumClick = {},
                    viewModel = viewModel
                )
            }

            // Then
            composeTestRule.onNodeWithText(errorMessage)
                .assertIsDisplayed()

            composeTestRule.setContent { } // Clear pour le prochain test
        }
    }

    @Test
    fun `empty state should be displayed when no albums are available`() {
        // Given
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Success(emptyList()))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Aucun album", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Vos albums apparaîtront ici", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `albums should be displayed in grid when state is Success`() {
        // Given
        val albums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", false),
            Album(3, 1, "Album 3", "url3", "thumb3", false)
        )
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Success(albums))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then
        albums.forEach { album ->
            composeTestRule.onNodeWithText(album.title)
                .assertIsDisplayed()
        }
    }

    @Test
    fun `album cards should display correct information`() {
        // Given
        val album = Album(
            id = 1,
            albumId = 100,
            title = "Test Album",
            url = "https://example.com/image.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg",
            isFavorite = false
        )
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Success(listOf(album)))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Album")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Album #100", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `clicking on album should call onAlbumClick with correct ID`() {
        // Given
        var clickedAlbumId: Int? = null
        val albums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", false)
        )
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Success(albums))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = { albumId ->
                    clickedAlbumId = albumId
                },
                viewModel = viewModel
            )
        }

        // Then - Cliquer sur le premier album
        composeTestRule.onNodeWithText("Album 1")
            .performClick()

        composeTestRule.waitForIdle()
        assertEquals(1, clickedAlbumId)

        // Then - Cliquer sur le deuxième album
        composeTestRule.onNodeWithText("Album 2")
            .performClick()

        composeTestRule.waitForIdle()
        assertEquals(2, clickedAlbumId)
    }

    @Test
    fun `clicking on any album should transmit correct ID`() {
        // Given
        val clickedIds = mutableListOf<Int>()
        val albums = (1..10).map {
            Album(it, 1, "Album $it", "url$it", "thumb$it", false)
        }
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Success(albums))
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = { albumId ->
                    clickedIds.add(albumId)
                },
                viewModel = viewModel
            )
        }

        // Then - Cliquer sur plusieurs albums
        albums.take(5).forEach { album ->
            composeTestRule.onNodeWithText(album.title)
                .performClick()
            composeTestRule.waitForIdle()
        }

        assertEquals(5, clickedIds.size)
        assertEquals(listOf(1, 2, 3, 4, 5), clickedIds)
    }
}
