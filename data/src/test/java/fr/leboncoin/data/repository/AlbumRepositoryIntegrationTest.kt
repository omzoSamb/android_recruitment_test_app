package fr.leboncoin.data.repository

import app.cash.turbine.test
import fr.leboncoin.data.database.AlbumDao
import fr.leboncoin.data.database.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AlbumRepositoryIntegrationTest {

    private lateinit var apiService: AlbumApiService
    private lateinit var albumDao: AlbumDao
    private lateinit var repository: AlbumRepository

    @Before
    fun setup() {
        apiService = mock()
        albumDao = mock()
        repository = AlbumRepository(apiService, albumDao)
    }

    @Test
    fun `getAllAlbums should return flow of albums from dao`() = runTest {
        // Given
        val entities = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumb1", false),
            AlbumEntity(2, 1, "Album 2", "url2", "thumb2", true)
        )
        whenever(albumDao.getAllAlbums()).thenReturn(flowOf(entities))

        // When
        val result = repository.getAllAlbums()

        // Then
        result.test {
            val albums = awaitItem()
            assertEquals("Should have 2 albums", 2, albums.size)
            assertEquals("First album title should match", "Album 1", albums[0].title)
            assertEquals("Second album should be favorite", true, albums[1].isFavorite)
        }
    }

    @Test
    fun `refreshAlbums should fetch from api and save to dao`() = runTest {
        // Given
        val dtos = listOf(
            AlbumDto(1, 1, "Album 1", "url1", "thumb1"),
            AlbumDto(2, 1, "Album 2", "url2", "thumb2")
        )
        whenever(apiService.getAlbums()).thenReturn(dtos)

        // When
        repository.refreshAlbums()

        // Then
        verify(apiService).getAlbums()
        verify(albumDao).insertAll(
            argThat { entities ->
                entities.size == 2 &&
                entities[0].id == 1 &&
                entities[1].id == 2
            }
        )
    }

    @Test
    fun `getAlbumById should return correct album`() = runTest {
        // Given
        val entity = AlbumEntity(1, 1, "Album 1", "url1", "thumb1", false)
        whenever(albumDao.getAlbumById(1)).thenReturn(entity)

        // When
        val result = repository.getAlbumById(1)

        // Then
        assertTrue("Result should not be null", result != null)
        assertEquals("Album ID should match", 1, result?.id)
        assertEquals("Album title should match", "Album 1", result?.title)
    }

    @Test
    fun `toggleFavorite should update favorite status`() = runTest {
        // Given
        val albumId = 1
        whenever(albumDao.isFavorite(albumId)).thenReturn(false)

        // When
        val result = repository.toggleFavorite(albumId)

        // Then
        assertTrue("Result should be true (new favorite status)", result)
        verify(albumDao).updateFavoriteStatus(albumId, true)
    }

    @Test
    fun `getFavoriteAlbums should return only favorite albums`() = runTest {
        // Given
        val favoriteEntities = listOf(
            AlbumEntity(1, 1, "Album 1", "url1", "thumb1", true),
            AlbumEntity(2, 1, "Album 2", "url2", "thumb2", true)
        )
        whenever(albumDao.getFavoriteAlbums()).thenReturn(flowOf(favoriteEntities))

        // When
        val result = repository.getFavoriteAlbums()

        // Then
        result.test {
            val albums = awaitItem()
            assertEquals("Should have 2 favorite albums", 2, albums.size)
            albums.forEach { album ->
                assertTrue("All albums should be favorites", album.isFavorite)
            }
        }
    }
}
