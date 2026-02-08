package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.adevinta.spark.components.image.Illustration
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.coreui.theme.MusicTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.DetailScreen
import fr.leboncoin.domain.model.Album

@AndroidEntryPoint
class DetailsActivity : ComponentActivity() {

    /*private val analyticsHelper: AnalyticsHelper by lazy {
        val dependencies = (application as AppDependenciesProvider).dependencies
        dependencies.analyticsHelper
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val albumJson = intent.getStringExtra("album")
        val album = Gson().fromJson(albumJson, Album::class.java)

        //analyticsHelper.initialize(this)
        //analyticsHelper.trackScreenView("Details")

        setContent {
            MusicTheme {
                album?.let {
                    DetailScreen(albumId = it.id)
                } ?: run {
                    Illustration(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.work_in_progress),
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                    )
                }
            }
        }
    }
}

