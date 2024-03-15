package com.oscarliang.gitfinder.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.AbsentLiveData
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.State
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: RepoRepository
) : ViewModel() {

    private val _query: MutableLiveData<Query> = MutableLiveData()
    private val nextPageHandler = NextPageHandler(repository, viewModelScope, Dispatchers.IO)

    val query: LiveData<Query>
        get() = _query

    val searchResults: LiveData<Resource<List<Repo>>> = _query.switchMap { input ->
        input.ifExists { query, number ->
            repository.search(
                query = query,
                number = number,
                coroutineScope = viewModelScope,
                ioDispatcher = Dispatchers.IO,
                mainDispatcher = Dispatchers.Main
            )
        }
    }

    val loadMoreStatus: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(query: String, number: Int) {
        val update = Query(query, number)
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

    class LoadMoreState(val isRunning: Boolean, val errorMessage: String?) {
        private var handledError = false

        val errorMessageIfNotHandled: String?
            get() {
                if (handledError) {
                    return null
                }
                handledError = true
                return errorMessage
            }
    }

    class NextPageHandler(
        private val repository: RepoRepository,
        private val coroutineScope: CoroutineScope,
        private val ioDispatcher: CoroutineDispatcher
    ) : Observer<Resource<Boolean>?> {

        val loadMoreState = MutableLiveData<LoadMoreState>()
        private var nextPageLiveData: LiveData<Resource<Boolean>?>? = null
        private var query: String? = null
        private var _hasMore: Boolean = false
        val hasMore
            get() = _hasMore

        init {
            reset()
        }

        fun queryNextPage(
            query: String,
            number: Int
        ) {
            if (this.query == query) {
                return
            }
            unregister()
            this.query = query
            nextPageLiveData =
                repository.searchNextPage(query, number, coroutineScope, ioDispatcher)
            loadMoreState.value = LoadMoreState(
                isRunning = true,
                errorMessage = null
            )
            nextPageLiveData?.observeForever(this)
        }

        override fun onChanged(value: Resource<Boolean>?) {
            if (value == null) {
                reset()
            } else {
                when (value.state) {
                    State.SUCCESS -> {
                        _hasMore = value.data == true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = null
                            )
                        )
                    }

                    State.ERROR -> {
                        _hasMore = true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = value.message
                            )
                        )
                    }

                    State.LOADING -> {
                        // ignore
                    }
                }
            }
        }

        private fun unregister() {
            nextPageLiveData?.removeObserver(this)
            nextPageLiveData = null
            if (_hasMore) {
                query = null
            }
        }

        fun reset() {
            unregister()
            _hasMore = true
            loadMoreState.value = LoadMoreState(
                isRunning = false,
                errorMessage = null
            )
        }
    }

}