package com.oscarliang.gitfinder.di

import androidx.room.Room
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.ui.bookmarks.BookmarksViewModel
import com.oscarliang.gitfinder.ui.search.SearchViewModel
import com.oscarliang.gitfinder.util.LiveDataCallAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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
        Room.databaseBuilder(androidContext(), GithubDatabase::class.java, "repo.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        val db = get<GithubDatabase>()
        db.repoDao()
    }

    single {
        RepoRepository(get(), get(), get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        BookmarksViewModel(get())
    }

}