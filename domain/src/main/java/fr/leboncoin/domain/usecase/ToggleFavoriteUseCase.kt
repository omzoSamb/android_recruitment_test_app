package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.repository.AlbumRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Boolean = repository.toggleFavorite(albumId)
}
