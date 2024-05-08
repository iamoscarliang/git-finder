package com.oscarliang.gitfinder.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.AbsentLiveData
import com.oscarliang.gitfinder.util.LoadMoreState
import com.oscarliang.gitfinder.util.NextPageHandler
import com.oscarliang.gitfinder.util.Resource
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel(
    private val repository: RepoRepository
) : ViewModel() {

    private val _query = MutableLiveData<Query>()
    val query: LiveData<Query>
        get() = _query

    val searchResults: LiveData<Resource<List<Repo>>> = _query.switchMap { input ->
        input.ifExists { query, number ->
            repository.search(query, number)
        }
    }

    private val nextPageHandler = NextPageHandler(repository)
    val loadMoreState: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(query: String, number: Int) {
        val trim = query.lowercase(Locale.getDefault()).trim()
        val update = Query(trim, number)
        if (_query.value == update) {
            return
        }
        nextPageHandler.reset()
        _query.value = update
    }

    fun loadNextPage() {
        _query.value?.let {
            if (it.query.isNotBlank()) {
                nextPageHandler.queryNextPage(it.query, it.number)
            }
        }
    }

    fun retryNextPage() {
        nextPageHandler.reset()
        loadNextPage()
    }

    fun retry() {
        _query.value?.let {
            _query.value = it
        }
    }

    fun toggleBookmark(repo: Repo) {
        val current = repo.bookmark
        val updated = repo.copy(bookmark = !current)
        viewModelScope.launch {
            repository.updateRepo(updated)
        }
    }

    data class Query(
        val query: String,
        val number: Int
    ) {
        fun <T> ifExists(f: (String, Int) -> LiveData<T>): LiveData<T> {
            return if (query.isBlank() || number == 0) {
                AbsentLiveData.create()
            } else {
                f(query, number)
            }
        }
    }

}