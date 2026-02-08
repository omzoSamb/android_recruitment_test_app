package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository
import javax.inject.Inject

class RefreshAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke() = repository.refreshAlbums()
}
