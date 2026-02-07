package fr.leboncoin.androidrecruitmenttestapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.leboncoin.androidrecruitmenttestapp.di.AppDependencies
import fr.leboncoin.androidrecruitmenttestapp.di.AppDependenciesProvider

@HiltAndroidApp
class PhotoApp : Application(), AppDependenciesProvider {

    override val dependencies: AppDependencies by lazy { AppDependencies(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PhotoApp
            private set
    }
}