package com.oscarliang.gitfinder.ui.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.repository.RepoRepository
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val repository: RepoRepository
) : ViewModel() {

    val bookmarks: LiveData<List<Repo>> = repository.getBookmarks()

    fun toggleBookmark(repo: Repo) {
        val current = repo.bookmark
        val updated = repo.copy(bookmark = !current)
        viewModelScope.launch {
            repository.updateRepo(updated)
        }
    }

}