package fr.leboncoin.androidrecruitmenttestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adevinta.spark.SparkTheme
import com.adevinta.spark.components.image.Illustration
import com.google.gson.Gson
import fr.leboncoin.androidrecruitmenttestapp.di.AppDependenciesProvider
import fr.leboncoin.androidrecruitmenttestapp.ui.AlbumItem
import fr.leboncoin.androidrecruitmenttestapp.utils.AnalyticsHelper
import fr.leboncoin.data.network.model.AlbumDto

class DetailsActivity : ComponentActivity() {

    private val analyticsHelper: AnalyticsHelper by lazy {
        val dependencies = (application as AppDependenciesProvider).dependencies
        dependencies.analyticsHelper
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val res = intent.getStringExtra("album")
        val album = Gson().fromJson(res, AlbumDto::class.java)

        analyticsHelper.initialize(this)
        analyticsHelper.trackScreenView("Details")

        setContent {
            SparkTheme {
                album ?.let {
                    AlbumItem(
                        album = it,
                        onItemSelected = {},
                    )
                }?: run {
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

