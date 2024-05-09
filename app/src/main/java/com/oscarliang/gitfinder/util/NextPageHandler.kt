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

    init {
        reset()
    }

    fun queryNextPage(
        query: String,
        number: Int
    ) {
        val current = loadMoreState.value
        if (current == null || current.isRunning || !current.hasMore) {
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
                    unregister()
                    loadMoreState.setValue(
                        LoadMoreState(
                            isRunning = false,
                            hasMore = value.data == true,
                            errorMessage = null
                        )
                    )
                }

                State.ERROR -> {
                    unregister()
                    loadMoreState.setValue(
                        LoadMoreState(
                            isRunning = false,
                            hasMore = false,
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

    fun reset() {
        unregister()
        loadMoreState.value = LoadMoreState(
            isRunning = false,
            hasMore = true,
            errorMessage = null
        )
    }

    private fun unregister() {
        nextPageLiveData?.removeObserver(this)
        nextPageLiveData = null
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