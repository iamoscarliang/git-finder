package com.oscarliang.gitfinder.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.oscarliang.gitfinder.model.Repo
import com.oscarliang.gitfinder.repository.RepoRepository
import com.oscarliang.gitfinder.util.MainDispatcherRule
import com.oscarliang.gitfinder.util.Resource
import com.oscarliang.gitfinder.util.TestUtil
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Rule
    @JvmField
    val instantExecutor = InstantTaskExecutorRule()

    private val repository = mockk<RepoRepository>(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun init() {
        viewModel = SearchViewModel(repository)
    }

    @Test
    fun empty() {
        val result = mockk<Observer<Resource<List<Repo>>>>()
        viewModel.searchResults.observeForever(result)
        viewModel.loadNextPage()
        verify { repository wasNot Called }
    }

    @Test
    fun basic() {
        val result = mockk<Observer<Resource<List<Repo>>>>()
        viewModel.searchResults.observeForever(result)
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10, any(), any(), any()) }
        verify { repository.searchNextPage("foo", 10, any(), any()) wasNot Called }
    }

    @Test
    fun noObserverNoQuery() = runTest {
        every { repository.searchNextPage("foo", 10, any(), any()) } returns mockk(relaxed = true)
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10, any(), any(), any()) wasNot Called }
        // Next page is user interaction and even if loading state is not observed, we query
        // would be better to avoid that if main search query is not observed
        viewModel.loadNextPage()
        verify { repository.searchNextPage("foo", 10, any(), any()) }
    }

    @Test
    fun swap() {
        val nextPage = MutableLiveData<Resource<Boolean>?>()
        every { repository.searchNextPage("foo", 10, any(), any()) } returns nextPage

        val result = mockk<Observer<Resource<List<Repo>>>>()
        viewModel.searchResults.observeForever(result)
        verify { repository wasNot Called }
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10, any(), any(), any()) }

        viewModel.loadNextPage()
        viewModel.loadMoreStatus.observeForever(mockk(relaxed = true))
        verify { repository.searchNextPage("foo", 10, any(), any()) }
        assertEquals(nextPage.hasActiveObservers(), true)
        viewModel.setQuery("bar", 10)
        assertEquals(nextPage.hasActiveObservers(), false)
        verify { repository.search("bar", 10, any(), any(), any()) }
        verify { repository.searchNextPage("bar", 10, any(), any()) wasNot Called }
    }

    @Test
    fun retry() {
        viewModel.retry()
        verify { repository wasNot Called }
        viewModel.setQuery("foo", 10)
        viewModel.retry()
        verify { repository wasNot Called }
        viewModel.searchResults.observeForever(mockk())
        verify { repository.search("foo", 10, any(), any(), any()) }
        clearMocks(repository)
        viewModel.retry()
        verify { repository.search("foo", 10, any(), any(), any()) }
    }

    @Test
    fun resetSameQuery() {
        viewModel.searchResults.observeForever(mockk())
        viewModel.setQuery("foo", 10)
        verify { repository.search("foo", 10, any(), any(), any()) }
        clearMocks(repository)
        viewModel.setQuery("foo", 10)
        verify { repository wasNot Called }
        viewModel.setQuery("bar", 10)
        verify { repository.search("bar", 10, any(), any(), any()) }
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