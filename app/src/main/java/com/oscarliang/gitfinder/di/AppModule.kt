package com.oscarliang.gitfinder.di

import android.view.View
import androidx.room.Room
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.util.LiveDataCallAdapterFactory
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(GithubService::class.java)
    }

    single {
        Room.databaseBuilder(get(), GithubDatabase::class.java, "repo.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { (db: GithubDatabase) ->
        db.repoDao()
    }

}