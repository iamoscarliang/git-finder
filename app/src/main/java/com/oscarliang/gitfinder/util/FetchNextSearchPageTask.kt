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
            val repos = response.items
            val bookmarks = db.repoDao().findBookmarks()
            repos.forEach { newData ->
                // We prevent overriding bookmark field
                newData.bookmark = bookmarks.any { currentData ->
                    currentData.id == newData.id
                }
            }

            // We merge all new search result into current result list
            val repoIds = mutableListOf<Int>()
            repoIds.addAll(current.repoIds)
            repoIds.addAll(repos.map { it.id })
            val merged = RepoSearchResult(
                query = query,
                count = response.count,
                repoIds = repoIds
            )
            db.repoDao().insertRepos(repos)
            db.repoDao().insertRepoSearchResult(merged)
            emit(Resource.success(true))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", true))
        }
    }

}