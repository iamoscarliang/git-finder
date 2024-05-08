package com.oscarliang.gitfinder.di

import androidx.room.Room
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.DB_NAME
import com.oscarliang.gitfinder.util.GITHUB_URL
import com.oscarliang.gitfinder.util.REFRESH_TIMEOUT
import com.oscarliang.gitfinder.util.RateLimiter
import com.oscarliang.gitfinder.ui.bookmarks.BookmarksViewModel
import com.oscarliang.gitfinder.ui.search.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl(GITHUB_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubService::class.java)
    }

    single {
        Room.databaseBuilder(androidContext(), GithubDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        val db = get<GithubDatabase>()
        db.repoDao()
    }

    single {
        RateLimiter<String>(REFRESH_TIMEOUT, TimeUnit.MINUTES)
    }

    single {
        RepoRepository(get(), get(), get(), get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        BookmarksViewModel(get())
    }

}