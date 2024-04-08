package com.oscarliang.gitfinder.util

import androidx.lifecycle.liveData
import com.oscarliang.gitfinder.api.GithubService
import com.oscarliang.gitfinder.db.GithubDatabase
import com.oscarliang.gitfinder.model.RepoSearchResult

class FetchNextSearchPageTask(
    private val query: String,
    private val number: Int,
    private val db: GithubDatabase,
    private val githubService: GithubService
) {

    fun asLiveData() = liveData {
        val current = db.repoDao().findRepoSearchResult(query)
        if (current == null) {
            emit(null)
            return@liveData
        }
        val currentCount = current.repoIds.size
        if (currentCount >= current.count) {
            emit(Resource.success(false))
            return@liveData
        }

        try {
            val response = githubService.searchRepos(
                query = query,
                number = number,
                page = currentCount / number + 1
            )
            val fetchedData = response.items

            // We merge all new search result into current result list
            val ids = arrayListOf<Int>()
            ids.addAll(current.repoIds)
            ids.addAll(fetchedData.map { it.id })
            val merged = RepoSearchResult(
                query = query,
                count = response.count,
                repoIds = ids
            )
            db.repoDao().insertRepos(fetchedData)
            db.repoDao().insertRepoSearchResult(merged)
            emit(Resource.success(true))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", true))
        }
    }

//    private val _liveData = MutableLiveData<Resource<Boolean>?>()
//    val liveData: MutableLiveData<Resource<Boolean>?> = _liveData
//
//    fun run() {
//        val result = db.repoDao().getSearchResult(query)
//        if (result == null || result.repoIds.isEmpty()) {
//            _liveData.postValue(null)
//            return
//        }
//        val current = result.repoIds.size
//        if (current % number != 0) {
//            _liveData.postValue(Resource.success(false))
//            return
//        }
//        val newValue = try {
//            val response = githubService.searchRepos(
//                query = query,
//                number = number,
//                page = current / number + 1
//            ).execute()
//            when (val apiResponse = ApiResponse.create(response)) {
//                is ApiSuccessResponse -> {
//                    // We merge all new search result into current result list
//                    val ids = arrayListOf<Int>()
//                    ids.addAll(result.repoIds)
//                    ids.addAll(apiResponse.body.items.map { it.id })
//                    val merged = RepoSearchResult(
//                        query,
//                        ids
//                    )
//                    db.runInTransaction {
//                        db.repoDao().insertRepos(apiResponse.body.items)
//                        db.repoDao().insertRepoSearchResults(merged)
//                    }
//                    Resource.success(apiResponse.body.items.size == number)
//                }
//
//                is ApiEmptyResponse -> {
//                    Resource.success(false)
//                }
//
//                is ApiErrorResponse -> {
//                    Resource.error(apiResponse.errorMessage, true)
//                }
//            }
//
//        } catch (e: IOException) {
//            Resource.error(e.message!!, true)
//        }
//        _liveData.postValue(newValue)
//    }

}