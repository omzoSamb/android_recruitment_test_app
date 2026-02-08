package fr.leboncoin.domain.usecase

import fr.leboncoin.domain.model.Album
import fr.leboncoin.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumByIdUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(id: Int): Album? = repository.getAlbumById(id)
}