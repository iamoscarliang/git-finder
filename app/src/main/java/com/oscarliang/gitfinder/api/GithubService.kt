package com.oscarliang.gitfinder.api

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GithubService {

    @GET("search/repositories")
    fun searchRepos(
        @Query("q") query: String,
        @Query("per_page") number: Int
    ): LiveData<ApiResponse<RepoSearchResponse>>

    @GET("search/repositories")
    fun searchRepos(
        @Query("q") query: String,
        @Query("per_page") number: Int,
        @Query("page") page: Int
    ): Call<RepoSearchResponse>

}