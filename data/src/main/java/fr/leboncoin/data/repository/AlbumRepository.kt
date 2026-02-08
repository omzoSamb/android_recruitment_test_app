package fr.leboncoin.data.repository

import fr.leboncoin.data.database.AlbumDao
import fr.leboncoin.data.database.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.domain.model.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import fr.leboncoin.domain.repository.AlbumRepository as IAlbumRepository

class AlbumRepository @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao
) : IAlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbums().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshAlbums() {
        try {
            val albumsFromApi = albumApiService.getAlbums()
            val albumEntities = albumsFromApi.map { it.toEntity() }
            albumDao.insertAll(albumEntities)
        } catch (e: Exception) {
            // Log error or handle appropriately
            throw e
        }
    }

    override suspend fun getAlbumById(id: Int): Album? {
        return albumDao.getAlbumById(id)?.toDomain()
    }

    override suspend fun toggleFavorite(albumId: Int): Boolean {
        val currentStatus = albumDao.isFavorite(albumId)
        val newStatus = !currentStatus
        albumDao.updateFavoriteStatus(albumId, newStatus)
        return newStatus
    }

    override fun getFavoriteAlbums(): Flow<List<Album>> {
        return albumDao.getFavoriteAlbums().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

fun AlbumDto.toEntity() = AlbumEntity(
    id = this.id,
    albumId = this.albumId,
    title = this.title,
    url = this.url,
    thumbnailUrl = this.thumbnailUrl,
    isFavorite = false
)

fun AlbumEntity.toDomain() = Album(
    id = this.id,
    albumId = this.albumId,
    title = this.title,
    url = this.url,
    thumbnailUrl = this.thumbnailUrl,
    isFavorite = this.isFavorite
)