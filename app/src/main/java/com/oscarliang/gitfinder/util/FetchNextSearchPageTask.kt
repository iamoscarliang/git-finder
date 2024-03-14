package com.oscarliang.gitfinder.util

import androidx.lifecycle.MutableLiveData
import com.oscarliang.gitfinder.api.ApiEmptyResponse
import com.oscarliang.gitfinder.api.ApiErrorResponse
import com.oscarliang.gitfinder.api.ApiResponse
import com.oscarliang.gitfinder.api.ApiSuccessResponse
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.model.RepoSearchResult
import java.io.IOException

class FetchNextSearchPageTask(
    private val query: String,
    private val number: Int,
    private val githubService: GithubService,
    private val db: GithubDatabase
) {

    private val _liveData = MutableLiveData<Resource<Boolean>?>()
    val liveData: MutableLiveData<Resource<Boolean>?> = _liveData

    fun run() {
        val result = db.repoDao().findSearchResult(query)
        if (result == null || result.repoIds.isEmpty()) {
            _liveData.postValue(null)
            return
        }
        val current = result.repoIds.size
        if (current % number != 0) {
            _liveData.postValue(Resource.success(false))
            return
        }
        val newValue = try {
            val response = githubService.searchRepos(
                query = query,
                number = number,
                page = current / number + 1
            ).execute()
            when (val apiResponse = ApiResponse.create(response)) {
                is ApiSuccessResponse -> {
                    // We merge all new search result into current result list
                    val ids = arrayListOf<Int>()
                    ids.addAll(result.repoIds)
                    ids.addAll(apiResponse.body.items.map { it.id })
                    val merged = RepoSearchResult(
                        query,
                        ids
                    )
                    db.runInTransaction {
                        db.repoDao().insertRepos(apiResponse.body.items)
                        db.repoDao().insertRepoSearchResults(merged)
                    }
                    Resource.success(apiResponse.body.items.size == number)
                }

                is ApiEmptyResponse -> {
                    Resource.success(false)
                }

                is ApiErrorResponse -> {
                    Resource.error(apiResponse.errorMessage, true)
                }
            }

        } catch (e: IOException) {
            Resource.error(e.message!!, true)
        }
        _liveData.postValue(newValue)
    }

}