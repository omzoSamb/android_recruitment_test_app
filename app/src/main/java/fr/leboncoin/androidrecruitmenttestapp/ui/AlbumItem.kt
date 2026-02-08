package fr.leboncoin.androidrecruitmenttestapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adevinta.spark.ExperimentalSparkApi
import fr.leboncoin.androidrecruitmenttestapp.coreui.components.AlbumCard
import fr.leboncoin.domain.model.Album

@OptIn(ExperimentalSparkApi::class)
@Composable
fun AlbumItem(
    album: Album,
    onItemSelected: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlbumCard(
        album = album,
        onClick = { onItemSelected(album) },
        modifier = modifier.padding(horizontal = 16.dp)
    )
}