package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ToggleFavoriteUseCaseTest {

    private lateinit var repository: AlbumRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `invoke should return true when album is added to favorites`() = runTest {
        // Given
        val albumId = 1
        whenever(repository.toggleFavorite(albumId)).thenReturn(true)

        // When
        val result = useCase(albumId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `invoke should return false when album is removed from favorites`() = runTest {
        // Given
        val albumId = 1
        whenever(repository.toggleFavorite(albumId)).thenReturn(false)

        // When
        val result = useCase(albumId)

        // Then
        assertFalse(result)
    }
}
