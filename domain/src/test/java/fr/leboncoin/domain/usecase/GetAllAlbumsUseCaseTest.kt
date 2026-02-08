package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetAllAlbumsUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: GetAllAlbumsUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = GetAllAlbumsUseCase(repository)
    }

    @Test
    fun `invoke should return flow of albums from repository`() = runTest {
        // Given
        val albums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", false)
        )
        whenever(repository.getAllAlbums()).thenReturn(flowOf(albums))

        // When
        val result = useCase()

        // Then
        result.collect { resultAlbums ->
            assertEquals(albums, resultAlbums)
        }
    }
}
