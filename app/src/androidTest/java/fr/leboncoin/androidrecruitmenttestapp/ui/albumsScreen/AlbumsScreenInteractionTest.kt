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
import fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen.createTestViewModelWithFavorites

class AlbumsScreenInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `clicking on album should transmit correct ID`() {
        // Given
        val clickedIds = mutableListOf<Int>()
        val albums = (1..5).map {
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

        // Then - Cliquer sur chaque album et vérifier l'ID
        albums.forEach { album ->
            composeTestRule.onNodeWithText(album.title)
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            assertEquals("Clicked ID should match album ID", album.id, clickedIds.last())
        }

        assertEquals("Should have clicked 5 albums", 5, clickedIds.size)
        assertEquals("All IDs should be correct", (1..5).toList(), clickedIds)
    }

    @Test
    fun `toggle favorites filter should show only favorites`() {
        // Given
        val allAlbums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", true),
            Album(3, 1, "Album 3", "url3", "thumb3", false)
        )
        val favoriteAlbums = listOf(Album(2, 1, "Album 2", "url2", "thumb2", true))

        val viewModel = createTestViewModelWithFavorites(
            allAlbums = allAlbums,
            favorites = favoriteAlbums
        )

        // When
        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        // Then - Vérifier que tous les albums sont affichés initialement
        allAlbums.forEach { album ->
            composeTestRule.onNodeWithText(album.title)
                .assertIsDisplayed()
        }

        // When - Activer le filtre favoris (simulé via le ViewModel)
        viewModel.toggleShowFavoritesOnly()
        composeTestRule.waitForIdle()

        // Then - Vérifier que seul l'album favori est affiché
        composeTestRule.onNodeWithText("Album 2")
            .assertIsDisplayed()
    }
}
