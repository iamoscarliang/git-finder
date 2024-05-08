package com.oscarliang.gitfinder.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.room.withTransaction
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.api.RepoSearchResponse
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.db.RepoDao
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.model.RepoSearchResult
import com.oscarliang.gitfinder.util.AbsentLiveData
import com.oscarliang.gitfinder.util.FetchNextSearchPageTask
import com.oscarliang.gitfinder.util.NetworkBoundResource
import com.oscarliang.gitfinder.util.RateLimiter
import com.oscarliang.gitfinder.util.Resource

class RepoRepository(
    private val db: GithubDatabase,
    private val repoDao: RepoDao,
    private val service: GithubService,
    private val rateLimiter: RateLimiter<String>
) {

    fun search(
        query: String,
        number: Int
    ): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>() {
            override suspend fun query(): List<Repo> {
                val result = repoDao.findRepoSearchResult(query)
                return if (result == null) {
                    listOf()
                } else {
                    repoDao.findReposById(result.repoIds)
                }
            }

            override fun queryObservable(): LiveData<List<Repo>> {
                return repoDao.getRepoSearchResult(query).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        repoDao.getOrdered(searchData.repoIds)
                    }
                }
            }

            override suspend fun fetch(): RepoSearchResponse {
                return service.searchRepos(query, number)
            }

            override suspend fun saveFetchResult(data: RepoSearchResponse) {
                val repos = data.items
                val bookmarks = repoDao.findBookmarks()
                repos.forEach { newData ->
                    // We prevent overriding bookmark field
                    newData.bookmark = bookmarks.any { currentData ->
                        currentData.id == newData.id
                    }
                }
                val repoIds = repos.map { it.id }
                val repoSearchResult = RepoSearchResult(
                    query = query,
                    count = data.count,
                    repoIds = repoIds
                )
                db.withTransaction {
                    repoDao.insertRepos(repos)
                    repoDao.insertRepoSearchResult(repoSearchResult)
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return rateLimiter.shouldFetch(query)
            }

            override fun onFetchFailed(exception: Exception) {
                rateLimiter.reset(query)
            }
        }.asLiveData()
    }

    fun searchNextPage(
        query: String,
        number: Int
    ): LiveData<Resource<Boolean>?> {
        return FetchNextSearchPageTask(
            query = query,
            number = number,
            db = db,
            repoDao = repoDao,
            service = service
        ).asLiveData()
    }

    fun getBookmarks(): LiveData<List<Repo>> {
        return repoDao.getBookmarks()
    }

    suspend fun updateRepo(repo: Repo) {
        repoDao.updateRepo(repo)
    }

}