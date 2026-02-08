package fr.leboncoin.androidrecruitmenttestapp.data

import fr.leboncoin.domain.model.Album
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumDataStructureTest {

    @Test
    fun `album should have all required fields`() {
        // Given & When
        val album = Album(
            id = 1,
            albumId = 100,
            title = "Test Album",
            url = "https://example.com/image.jpg",
            thumbnailUrl = "https://example.com/thumb.jpg",
            isFavorite = false
        )

        // Then
        assertNotNull("ID should not be null", album.id)
        assertNotNull("Album ID should not be null", album.albumId)
        assertNotNull("Title should not be null", album.title)
        assertNotNull("URL should not be null", album.url)
        assertNotNull("Thumbnail URL should not be null", album.thumbnailUrl)
        assertNotNull("IsFavorite should not be null", album.isFavorite)
    }

    @Test
    fun `album list should contain valid albums`() {
        // Given
        val albums = listOf(
            Album(1, 1, "Album 1", "url1", "thumb1", false),
            Album(2, 1, "Album 2", "url2", "thumb2", true),
            Album(3, 2, "Album 3", "url3", "thumb3", false)
        )

        // When & Then
        albums.forEach { album ->
            assertTrue("ID should be positive", album.id > 0)
            assertTrue("Album ID should be positive", album.albumId > 0)
            assertTrue("Title should not be empty", album.title.isNotEmpty())
            assertTrue("URL should not be empty", album.url.isNotEmpty())
            assertTrue("Thumbnail URL should not be empty", album.thumbnailUrl.isNotEmpty())
        }
    }

    @Test
    fun `album data should match expected structure`() {
        // Given
        val expectedId = 123
        val expectedAlbumId = 456
        val expectedTitle = "Expected Title"
        val expectedUrl = "https://example.com/expected.jpg"
        val expectedThumbnailUrl = "https://example.com/expected_thumb.jpg"
        val expectedIsFavorite = true

        // When
        val album = Album(
            id = expectedId,
            albumId = expectedAlbumId,
            title = expectedTitle,
            url = expectedUrl,
            thumbnailUrl = expectedThumbnailUrl,
            isFavorite = expectedIsFavorite
        )

        // Then
        assertEquals("ID should match", expectedId, album.id)
        assertEquals("Album ID should match", expectedAlbumId, album.albumId)
        assertEquals("Title should match", expectedTitle, album.title)
        assertEquals("URL should match", expectedUrl, album.url)
        assertEquals("Thumbnail URL should match", expectedThumbnailUrl, album.thumbnailUrl)
        assertEquals("IsFavorite should match", expectedIsFavorite, album.isFavorite)
    }

    @Test
    fun `album list should return correct number of items`() {
        // Given
        val albums = (1..10).map {
            Album(it, 1, "Album $it", "url$it", "thumb$it", false)
        }

        // When & Then
        assertEquals("Should have 10 albums", 10, albums.size)
        assertEquals("First album ID should be 1", 1, albums[0].id)
        assertEquals("Last album ID should be 10", 10, albums[9].id)
    }

    @Test
    fun `album should handle favorite status correctly`() {
        // Given
        val albumNotFavorite = Album(1, 1, "Album", "url", "thumb", false)
        val albumFavorite = Album(2, 1, "Album", "url", "thumb", true)

        // When & Then
        assertFalse("Album should not be favorite", albumNotFavorite.isFavorite)
        assertTrue("Album should be favorite", albumFavorite.isFavorite)
    }

    @Test
    fun `album should have valid URL format`() {
        // Given
        val albums = listOf(
            Album(1, 1, "Album 1", "https://example.com/image1.jpg", "https://example.com/thumb1.jpg", false),
            Album(2, 1, "Album 2", "http://example.com/image2.jpg", "http://example.com/thumb2.jpg", false)
        )

        // When & Then
        albums.forEach { album ->
            assertTrue("URL should start with http",
                album.url.startsWith("http://") || album.url.startsWith("https://"))
            assertTrue("Thumbnail URL should start with http",
                album.thumbnailUrl.startsWith("http://") || album.thumbnailUrl.startsWith("https://"))
        }
    }
}
