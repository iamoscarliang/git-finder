package com.oscarliang.gitfinder.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GithubService {

    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("per_page") number: Int
    ): RepoSearchResponse

    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("per_page") number: Int,
        @Query("page") page: Int
    ): RepoSearchResponse

}