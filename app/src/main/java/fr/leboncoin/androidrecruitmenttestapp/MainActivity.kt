package fr.leboncoin.androidrecruitmenttestapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.coreui.theme.MusicTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.albumsScreen.AlbumsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicTheme {
                AlbumsScreen(
                    onAlbumClick = { albumId ->
                        val intent = Intent(this, DetailsActivity::class.java).apply {
                            putExtra("albumId", albumId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}