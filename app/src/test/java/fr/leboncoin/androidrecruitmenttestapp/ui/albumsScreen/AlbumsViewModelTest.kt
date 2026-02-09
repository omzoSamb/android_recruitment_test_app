package fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen

import android.content.Context
import fr.leboncoin.domain.usecase.GetAllAlbumsUseCase
import fr.leboncoin.domain.usecase.GetFavoriteAlbumsUseCase
import fr.leboncoin.domain.usecase.RefreshAlbumsUseCase
import fr.leboncoin.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AlbumsViewModelTest {

    private lateinit var getAllAlbumsUseCase: GetAllAlbumsUseCase
    private lateinit var getFavoriteAlbumsUseCase: GetFavoriteAlbumsUseCase
    private lateinit var refreshAlbumsUseCase: RefreshAlbumsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var viewModel: AlbumsViewModel

    @Before
    fun setup() {
        getAllAlbumsUseCase = mock()
        getFavoriteAlbumsUseCase = mock()
        refreshAlbumsUseCase = mock()
        toggleFavoriteUseCase = mock()
    }

    @Test
    fun toggleFavorite_should_call_use_case_with_correct_album_ID() = runTest {
        // Given
        val albumId = 123
        whenever(getAllAlbumsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(toggleFavoriteUseCase(albumId)).thenReturn(true)

        viewModel = AlbumsViewModel(
            context = mock(),
            getAllAlbumsUseCase = getAllAlbumsUseCase,
            getFavoriteAlbumsUseCase = getFavoriteAlbumsUseCase,
            refreshAlbumsUseCase = refreshAlbumsUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )

        // When
        viewModel.toggleFavorite(albumId)

        // Then
        verify(toggleFavoriteUseCase).invoke(albumId)
    }
}
