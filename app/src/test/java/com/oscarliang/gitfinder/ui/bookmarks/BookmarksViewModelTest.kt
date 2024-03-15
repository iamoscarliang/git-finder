package com.oscarliang.gitfinder.ui.bookmarks

import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.MainDispatcherRule
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BookmarksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<RepoRepository>(relaxed = true)
    private lateinit var viewModel: BookmarksViewModel

    @Before
    fun init() {
        viewModel = BookmarksViewModel(repository)
    }

    @Test
    fun update() = runTest {
        val current = TestUtil.createRepo("a", "b", "c")
        val updated = current.copy(bookmark = true)
        viewModel.toggleBookmark(current)
        advanceUntilIdle()   // Yields to perform the registrations
        coVerify { repository.updateRepo(updated) }
    }

}