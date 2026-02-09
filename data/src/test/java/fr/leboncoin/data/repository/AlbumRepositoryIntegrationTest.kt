package fr.leboncoin.data.repository

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
    fun refreshAlbums_should_fetch_from_api_and_save_to_dao() = runTest {
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
    fun getAlbumById_should_return_correct_album() = runTest {
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
    fun toggleFavorite_should_update_favorite_status() = runTest {
        // Given
        val albumId = 1
        whenever(albumDao.isFavorite(albumId)).thenReturn(false)

        // When
        val result = repository.toggleFavorite(albumId)

        // Then
        assertTrue("Result should be true (new favorite status)", result)
        verify(albumDao).updateFavoriteStatus(albumId, true)
    }

}
