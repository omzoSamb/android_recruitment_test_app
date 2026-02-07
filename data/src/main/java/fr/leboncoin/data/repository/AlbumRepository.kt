package fr.leboncoin.data.repository

import fr.leboncoin.data.database.AlbumDao
import fr.leboncoin.data.database.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlbumRepository(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao
) {
    val albums: Flow<List<AlbumDto>> = albumDao.getAllAlbums().map { entities ->
        entities.map { it.toDto() }
    }
    //suspend fun getAllAlbums() = albumApiService.getAlbums()

    suspend fun getAllAlbums() {
        try {
            // 1. Récupérer les albums depuis l'API
            val albumsFromApi = albumApiService.getAlbums()
            // 2. Les transformer en entités pour la base de données
            val albumEntities = albumsFromApi.map { it.toEntity() }
            // 3. Les insérer dans Room
            albumDao.insertAll(albumEntities)
        } catch (e: Exception) {
        }
    }
}

fun AlbumDto.toEntity() = AlbumEntity(
    id = this.id,
    albumId = this.albumId,
    title = this.title,
    url = this.url,
    thumbnailUrl = this.thumbnailUrl
)

fun AlbumEntity.toDto() = AlbumDto(
    id = this.id,
    albumId = this.albumId,
    title = this.title,
    url = this.url,
    thumbnailUrl = this.thumbnailUrl
)