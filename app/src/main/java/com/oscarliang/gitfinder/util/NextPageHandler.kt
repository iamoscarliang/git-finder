package com.oscarliang.gitfinder.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.gitfinder.repository.RepoRepository

class NextPageHandler(
    private val repository: RepoRepository
) : Observer<Resource<Boolean>?> {

    val loadMoreState = MutableLiveData<LoadMoreState>()
    private var nextPageLiveData: LiveData<Resource<Boolean>?>? = null
    private var _hasMore: Boolean = false
    val hasMore: Boolean
        get() = _hasMore

    init {
        reset()
    }

    fun queryNextPage(
        query: String,
        number: Int
    ) {
        if (!_hasMore) {
            return
        }
        unregister()
        nextPageLiveData = repository.searchNextPage(query, number)
        loadMoreState.value = LoadMoreState(
            isRunning = true,
            hasMore = true,
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
                            hasMore = _hasMore,
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
                            hasMore = true,
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
    }

    fun reset() {
        unregister()
        _hasMore = true
        loadMoreState.value = LoadMoreState(
            isRunning = false,
            hasMore = true,
            errorMessage = null
        )
    }
}

data class LoadMoreState(
    val isRunning: Boolean,
    val hasMore: Boolean,
    val errorMessage: String?
) {
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