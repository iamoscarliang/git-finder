package com.oscarliang.gitfinder

import android.app.Application
import com.oscarliang.gitfinder.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GithubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GithubApplication)
            modules(appModule)
        }
    }

}