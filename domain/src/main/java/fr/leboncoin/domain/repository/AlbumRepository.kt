package fr.leboncoin.domain.repository

import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun refreshAlbums()
    suspend fun getAlbumById(id: Int): Album?
    suspend fun toggleFavorite(albumId: Int): Boolean
    fun getFavoriteAlbums(): Flow<List<Album>>
}
