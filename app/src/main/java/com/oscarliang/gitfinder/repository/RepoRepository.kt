package com.oscarliang.gitfinder.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.oscarliang.gitfinder.api.ApiResponse
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RepoRepository(
    private val db: GithubDatabase,
    private val repoDao: RepoDao,
    private val githubService: GithubService
) {

    private val repoRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun search(
        query: String,
        number: Int,
        coroutineScope: CoroutineScope
    ): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(coroutineScope) {
            override fun saveCallResult(item: RepoSearchResponse) {
                val repoIds = item.items.map { it.id }
                val repoSearchResult = RepoSearchResult(
                    query = query,
                    repoIds = repoIds
                )
                db.runInTransaction {
                    repoDao.deleteSearchResult(query)
                    repoDao.insertRepos(item.items)
                    repoDao.insertRepoSearchResults(repoSearchResult)
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || repoRateLimit.shouldFetch(query)
            }

            override fun loadFromDb(): LiveData<List<Repo>> {
                return repoDao.search(query).switchMap { searchData ->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        repoDao.getOrdered(searchData.repoIds)
                    }
                }
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> {
                return githubService.searchRepos(query, number)
            }
        }.asLiveData()
    }

    fun searchNextPage(
        query: String,
        number: Int,
        coroutineScope: CoroutineScope
    ): LiveData<Resource<Boolean>?> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            number = number,
            githubService = githubService,
            db = db
        )
        coroutineScope.launch(Dispatchers.IO) {
            fetchNextSearchPageTask.run()
        }
        return fetchNextSearchPageTask.liveData
    }

    fun getBookmarks(): LiveData<List<Repo>> {
        return repoDao.getBookmarks()
    }

    suspend fun updateRepo(repo: Repo) {
        repoDao.updateRepo(repo)
    }

}