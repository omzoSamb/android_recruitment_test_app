package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.adevinta.spark.components.text.Text
import dagger.hilt.android.AndroidEntryPoint
import fr.leboncoin.androidrecruitmenttestapp.coreui.theme.MusicTheme
import fr.leboncoin.androidrecruitmenttestapp.ui.detailScreen.DetailScreen

@AndroidEntryPoint
class DetailsActivity : ComponentActivity() {

    /*private val analyticsHelper: AnalyticsHelper by lazy {
        val dependencies = (application as AppDependenciesProvider).dependencies
        dependencies.analyticsHelper
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val albumId = intent.getIntExtra("albumId", -1)

        //analyticsHelper.initialize(this)
        //analyticsHelper.trackScreenView("Details")
        setContent {
            MusicTheme {
                if (albumId != -1) {
                    DetailScreen(albumId = albumId)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Album introuvable")
                    }
                }
            }
        }
    }
}

