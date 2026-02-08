package fr.leboncoin.androidrecruitmenttestapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.coreui.theme.MusicTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.AlbumsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicTheme {
                AlbumsScreen(
                    onItemSelected = { album ->
                        val res = Gson().toJson(album)
                        val intent = Intent(this, DetailsActivity::class.java).apply {
                            putExtra("album", res)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}